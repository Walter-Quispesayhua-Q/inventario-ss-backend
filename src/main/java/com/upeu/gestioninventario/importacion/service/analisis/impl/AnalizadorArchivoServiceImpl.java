package com.upeu.gestioninventario.importacion.service.analisis.impl;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.importacion.dto.EstadisticasImportacionDTO;
import com.upeu.gestioninventario.importacion.dto.FilaPreviewDTO;
import com.upeu.gestioninventario.importacion.dto.ResponsableConEstacionesDTO;
import com.upeu.gestioninventario.importacion.dto.SesionAnalisisV3DTO;
import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.dto.formato.clavevalor.FormatoClaveValorDTO;
import com.upeu.gestioninventario.importacion.dto.formato.matriz.FormatoMatrizDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.HojaAnalizadaDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.RespuestaImportacionDTO;
import com.upeu.gestioninventario.importacion.dto.formato.tabular.FormatoTabularDTO;
import com.upeu.gestioninventario.importacion.service.analisis.IAnalizadorArchivo;
import com.upeu.gestioninventario.importacion.service.analisis.IDetectorFormato;
import com.upeu.gestioninventario.importacion.service.analisis.IFormatoConfigLoader;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.LectorClaveValorService;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.LectorMatrizService;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.LectorTabularService;
import com.upeu.gestioninventario.importacion.service.cache.ISesionAnalisisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalizadorArchivoServiceImpl implements IAnalizadorArchivo {

    private final IDetectorFormato detectorFormato;
    private final IFormatoConfigLoader formatoConfigLoader;
    private final ISesionAnalisisCache sesionAnalisisCache;

    private final LectorTabularService lectorTabular;
    private final LectorClaveValorService lectorClaveValor;
    private final LectorMatrizService lectorMatriz;

    @Override
    public RespuestaImportacionDTO analizarArchivo(MultipartFile archivo) throws IOException {
        log.info("Iniciando análisis optimizado del archivo: {}", archivo.getOriginalFilename());
        String nombreArchivo = archivo.getOriginalFilename();

        try {
            Map<Integer, IDetectorFormato.ResultadoDeteccionHoja> resultadosPorHoja =
                    detectorFormato.detectarFormatoTodasHojas(archivo);

            if (resultadosPorHoja.isEmpty()) {
                return RespuestaImportacionDTO.archivoInvalido(nombreArchivo, "El archivo no contiene hojas válidas");
            }

            List<HojaAnalizadaDTO> hojasAnalizadas = new ArrayList<>();
            Map<String, Integer> categoriasDetectadas = new HashMap<>();

            try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
                for (Map.Entry<Integer, IDetectorFormato.ResultadoDeteccionHoja> entry : resultadosPorHoja.entrySet()) {
                    int indiceHoja = entry.getKey();
                    IDetectorFormato.ResultadoDeteccionHoja resultado = entry.getValue();
                    Sheet hoja = workbook.getSheetAt(indiceHoja);

                    log.info("Procesando hoja {} - Formato: {}", resultado.nombreHoja(), resultado.formatoDetectado());

                    HojaAnalizadaDTO hojaAnalizada = procesarHojaSegunFormato(hoja, resultado, indiceHoja);

                    if (hojaAnalizada != null) {
                        hojasAnalizadas.add(hojaAnalizada);

                        if (hojaAnalizada.filasEnriquecidas() != null) {
                            for (FilaPreviewDTO fila : hojaAnalizada.filasEnriquecidas()) {
                                categoriasDetectadas.merge(fila.categoriaDetectada(), 1, Integer::sum);
                            }
                        }
                    }
                }
            }

            SesionAnalisisV3DTO sesionV3 = new SesionAnalisisV3DTO(
                    null,
                    nombreArchivo,
                    hojasAnalizadas,
                    categoriasDetectadas
            );

            String sessionId = sesionAnalisisCache.guardarSesion(sesionV3);

            log.info("SesionV3 creada: {} - {} hojas, {} filas, {} estaciones",
                    sessionId, hojasAnalizadas.size(), 
                    sesionV3.todasLasFilas().size(), 
                    sesionV3.todasLasEstaciones().size());

            TipoFormatoDetectado formatoPrincipal = resultadosPorHoja.get(0).formatoDetectado();
            boolean esFormatoFichas = formatoPrincipal == TipoFormatoDetectado.MATRIZ;
            String tipoConteo = esFormatoFichas ? "FICHAS" : "FILAS";
            
            List<FilaPreviewDTO> todasLasFilas = sesionV3.todasLasFilas();
            List<EstacionConComponentesDTO> todasLasEstaciones = sesionV3.todasLasEstaciones();
            Integer totalEstacionesConteo = esFormatoFichas ? todasLasEstaciones.size() : null;

            EstadisticasImportacionDTO estadisticas = new EstadisticasImportacionDTO(
                    todasLasFilas.size(),
                    (int) todasLasFilas.stream().filter(FilaPreviewDTO::esValida).count(),
                    hojasAnalizadas.size(),
                    categoriasDetectadas,
                    calcularConfianzaPromedio(todasLasFilas),
                    totalEstacionesConteo,
                    tipoConteo
            );

            List<ResponsableConEstacionesDTO> estacionesPorResponsable = agruparEstacionesPorResponsable(todasLasEstaciones);

            return RespuestaImportacionDTO.exito(
                    nombreArchivo,
                    sessionId,
                    hojasAnalizadas,
                    estacionesPorResponsable,
                    estadisticas
            );

        } catch (Exception e) {
            log.error("Error al analizar archivo: {}", e.getMessage(), e);
            return RespuestaImportacionDTO.archivoInvalido(nombreArchivo, "Error al procesar: " + e.getMessage());
        }
    }

    private HojaAnalizadaDTO procesarHojaSegunFormato(
            Sheet hoja,
            IDetectorFormato.ResultadoDeteccionHoja resultado,
            int indiceHoja) {

        return switch (resultado.formatoDetectado()) {
            case TABULAR -> {
                log.info("Usando LectorTabularService para hoja: {}", hoja.getSheetName());
                FormatoTabularDTO config = formatoConfigLoader.obtenerFormatoTabular();
                yield lectorTabular.extraerDatos(hoja, config, indiceHoja);
            }
            case CLAVE_VALOR -> {
                log.info("Usando LectorClaveValorService para hoja: {}", hoja.getSheetName());
                FormatoClaveValorDTO config = formatoConfigLoader.obtenerFormatoClaveValor();
                yield lectorClaveValor.extraerDatos(hoja, config, indiceHoja);
            }
            case MATRIZ -> {
                log.info("Usando LectorMatrizService para hoja: {}", hoja.getSheetName());
                FormatoMatrizDTO config = formatoConfigLoader.obtenerFormatoMatriz();
                yield lectorMatriz.extraerDatos(hoja, config, indiceHoja);
            }
            case DESCONOCIDO -> {
                log.warn("Formato desconocido para hoja: {}. Intentando como TABULAR", hoja.getSheetName());
                FormatoTabularDTO config = formatoConfigLoader.obtenerFormatoTabular();
                yield lectorTabular.extraerDatos(hoja, config, indiceHoja);
            }
        };
    }

    private double calcularConfianzaPromedio(List<FilaPreviewDTO> filas) {
        if (filas.isEmpty()) return 0.0;
        return filas.stream()
                .mapToDouble(FilaPreviewDTO::confianzaCategoria)
                .average()
                .orElse(0.0);
    }

    private List<ResponsableConEstacionesDTO> agruparEstacionesPorResponsable(
            List<EstacionConComponentesDTO> estaciones) {

        if (estaciones == null || estaciones.isEmpty()) {
            return List.of();
        }

        Map<String, List<EstacionConComponentesDTO>> agrupadas = estaciones.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getResponsableDetectado() != null
                                ? e.getResponsableDetectado()
                                : "Sin responsable detectado",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return agrupadas.entrySet().stream()
                .map(entry -> ResponsableConEstacionesDTO.crear(
                        entry.getKey(),
                        false,
                        entry.getValue()
                ))
                .toList();
    }
}