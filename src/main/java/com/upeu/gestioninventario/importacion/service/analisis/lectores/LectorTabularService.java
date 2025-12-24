package com.upeu.gestioninventario.importacion.service.analisis.lectores;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.importacion.dto.ColumnaDetectadaDTO;
import com.upeu.gestioninventario.importacion.dto.FilaPreviewDTO;
import com.upeu.gestioninventario.importacion.dto.ResultadoDeteccionCategoria;
import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.EstructuraHojaDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.FormatoGridDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.HojaAnalizadaDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.MetadatosHojaDTO;
import com.upeu.gestioninventario.importacion.dto.formato.tabular.FormatoTabularDTO;
import com.upeu.gestioninventario.importacion.service.analisis.IBuscadorAmbiente;
import com.upeu.gestioninventario.importacion.service.analisis.IDetectorCategoria;
import com.upeu.gestioninventario.importacion.service.analisis.IDetectorCeldaCruda;
import com.upeu.gestioninventario.importacion.service.analisis.IExtractorAtributos;
import com.upeu.gestioninventario.importacion.service.analisis.IMapeadorDatos;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.builder.EstacionBuilder;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.util.ExcelCellReader;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.util.HeaderDetector;
import com.upeu.gestioninventario.ml.service.PlantillaLoaderService;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class LectorTabularService extends AbstractLectorFormato implements ILectorFormato<FormatoTabularDTO> {

    private final PlantillaLoaderService plantillaLoaderService;
    private final IDetectorCeldaCruda detectorCeldaCruda;

    public LectorTabularService(
            IMapeadorDatos mapeadorDatos,
            IDetectorCategoria detectorCategoria,
            IExtractorAtributos extractorAtributos,
            IBuscadorAmbiente buscadorAmbiente,
            ExcelCellReader cellReader,
            HeaderDetector headerDetector,
            EstacionBuilder estacionBuilder,
            IDetectorCeldaCruda detectorCeldaCruda,
            PlantillaLoaderService plantillaLoaderService) {
        super(mapeadorDatos, detectorCategoria, extractorAtributos, buscadorAmbiente, cellReader, headerDetector, estacionBuilder);
        this.detectorCeldaCruda = detectorCeldaCruda;
        this.plantillaLoaderService = plantillaLoaderService;
    }

    @Override
    public HojaAnalizadaDTO extraerDatos(Sheet hoja, FormatoTabularDTO config, int indiceHoja) {
        log.info("Extrayendo datos TABULAR de hoja: {}", hoja.getSheetName());

        int filasAEscanear = 10;
        double umbralDensidad = 0.5;
        Integer filaConfigHeaders = null;

        if (config.encabezados() != null) {
            filaConfigHeaders = config.encabezados().fila();
            if (config.encabezados().filasAEscanear() != null) {
                filasAEscanear = config.encabezados().filasAEscanear();
            }
            if (config.encabezados().umbralDensidad() != null) {
                umbralDensidad = config.encabezados().umbralDensidad();
            }
        }

        int filaHeaders = filaConfigHeaders != null
                ? filaConfigHeaders
                : detectarFilaHeaders(hoja, filasAEscanear, umbralDensidad);

        log.info("Headers detectados en fila: {}", filaHeaders + 1);

        Map<String, String> metadatosHoja = new LinkedHashMap<>();
        if (filaHeaders > 0) {
            metadatosHoja = extraerMetadatosGenericos(hoja, 0, filaHeaders);
            log.info("Metadatos extraídos antes de headers: {}", metadatosHoja);
        }

        Row rowHeaders = hoja.getRow(filaHeaders);
        int numColumnas = detectarNumeroColumnas(hoja, 10);
        List<String> headers = leerFilaCompleta(rowHeaders, numColumnas);
        List<ColumnaDetectadaDTO> columnasMapeadas = mapeadorDatos.mapearNombres(headers);

        int filaInicio = filaHeaders + 1;
        int filaFin = hoja.getLastRowNum();

        if (config.datos() != null) {
            if (config.datos().filaInicio() != null) {
                filaInicio = config.datos().filaInicio();
            }
            if (config.datos().filaFin() != null) {
                filaFin = config.datos().filaFin();
            }
        }

        // Detectar si es formato de celda cruda (datos concatenados en una columna)
        List<List<String>> filasMuestra = obtenerFilasMuestra(hoja, filaInicio, Math.min(filaFin, filaInicio + 5), numColumnas);
        boolean esModoCeldaCruda = detectorCeldaCruda.esFormatoCeldaCruda(headers, filasMuestra);
        int columnaDatosCrudos = esModoCeldaCruda ? detectorCeldaCruda.obtenerColumnaConDatos(headers, filasMuestra) : -1;
        
        if (esModoCeldaCruda) {
            log.info("Formato CELDA CRUDA detectado. Columna principal de datos: {}", columnaDatosCrudos);
        }

        List<FilaPreviewDTO> filasEnriquecidas = new ArrayList<>();
        List<Map<String, String>> filasTabular = new ArrayList<>();

        for (int i = filaInicio; i <= filaFin; i++) {
            Row fila = hoja.getRow(i);
            if (fila == null || esFilaVacia(fila)) continue;

            List<String> valores = leerFilaCompleta(fila, numColumnas);

            Map<String, String> valoresOriginales = new LinkedHashMap<>();
            for (int col = 0; col < Math.min(headers.size(), valores.size()); col++) {
                valoresOriginales.put(headers.get(col), valores.get(col));
            }

            filasTabular.add(valoresOriginales);

            String textoCompleto = String.join(" ", valores);
            ResultadoDeteccionCategoria categoria = detectorCategoria.detectarCategoria(textoCompleto);

            Map<String, String> atributosExtraidos = new HashMap<>();
            String categoriaDetectada = "Desconocida";
            double confianza = 0.0;

            if (categoria != null && categoria.plantilla() != null) {
                categoriaDetectada = categoria.plantilla().nombrePlantilla();
                confianza = categoria.confianza();

                PlantillaSeedDTO plantilla = plantillaLoaderService.getPlantillasCache()
                        .get(categoria.plantilla().idPlantillaSeed());

                if (plantilla != null) {
                    Map<String, String> datosPorColumna = new HashMap<>();
                    for (int col = 0; col < columnasMapeadas.size(); col++) {
                        if (columnasMapeadas.get(col).mapeada() && col < valores.size()) {
                            datosPorColumna.put(columnasMapeadas.get(col).campoMapeado().toLowerCase(), valores.get(col));
                        }
                    }
                    atributosExtraidos = extractorAtributos.extraerAtributos(textoCompleto, datosPorColumna, plantilla);
                }
            }

            filasEnriquecidas.add(construirFilaPreview(i + 1, valoresOriginales, atributosExtraidos, categoriaDetectada, confianza));
        }


        List<EstacionConComponentesDTO> estacionesSugeridas = new ArrayList<>();

        // En modo celda cruda, cada fila es un bien individual - NO agrupar en estaciones
        if (esModoCeldaCruda) {
            log.info("Modo CELDA CRUDA: {} bienes individuales detectados (sin agrupación en estaciones)", 
                    filasEnriquecidas.size());
        } else if (config.fichasMultiples() != null && config.fichasMultiples().habilitadas()) {
            Map<String, List<FilaPreviewDTO>> gruposPorFicha = agruparPorFichas(filasEnriquecidas, config.fichasMultiples());

            // Solo crear estaciones si se detectaron fichas reales (no solo FICHA-DEFAULT)
            // Y si no son fichas individuales (cada CAF es una ficha = no es agrupación real)
            boolean hayFichasReales = gruposPorFicha.size() > 1 && gruposPorFicha.size() < filasEnriquecidas.size() / 2;
            boolean noEsListaSimple = gruposPorFicha.values().stream().anyMatch(l -> l.size() > 1);
            
            if (hayFichasReales && noEsListaSimple) {
                int indiceEstacion = 1;
                for (Map.Entry<String, List<FilaPreviewDTO>> entry : gruposPorFicha.entrySet()) {
                    AmbienteDetectadoDTO ambiente = entry.getValue().isEmpty()
                            ? AmbienteDetectadoDTO.noDetectado()
                            : entry.getValue().get(0).ambienteDetectado();

                    EstacionConComponentesDTO estacion = crearEstacionDesdeGrupo(
                            entry.getKey(), entry.getValue(), ambiente, indiceEstacion++);

                    if (estacion != null) {
                        String responsable = entry.getValue().stream()
                                .map(f -> extraerResponsableDeDatos(f.valoresOriginales()))
                                .filter(r -> r != null && !r.isBlank())
                                .findFirst()
                                .orElse(null);
                        estacion.setResponsableDetectado(responsable);
                        estacionesSugeridas.add(estacion);
                    }
                }

                log.info("Se detectaron {} estacion(es) con fichas reales en formato TABULAR", estacionesSugeridas.size());
            } else {
                log.info("Lista simple detectada: {} bienes individuales (sin agrupación en estaciones)", 
                        filasEnriquecidas.size());
            }
        }

        MetadatosHojaDTO metadatos = construirMetadatosHoja(
                TipoFormatoDetectado.TABULAR,
                hoja.getSheetName(),
                indiceHoja,
                filasEnriquecidas.size(),
                numColumnas,
                85  // Score de detección
        );

        EstructuraHojaDTO estructura = construirEstructuraTabular(headers);

        FormatoGridDTO formatoGrid = construirFormatoGrid(
                filaInicio, filaFin, 0, numColumnas - 1, filaHeaders
        );

        return HojaAnalizadaDTO.paraTabular(
                metadatos,
                estructura,
                formatoGrid,
                filasTabular,
                filasEnriquecidas,
                estacionesSugeridas
        );
    }

    @Override
    public boolean soportaFormato(FormatoTabularDTO config) {
        return config != null && "TABULAR".equals(config.tipoFormato());
    }

    private List<List<String>> obtenerFilasMuestra(Sheet hoja, int filaInicio, int filaFin, int numColumnas) {
        List<List<String>> muestra = new ArrayList<>();
        for (int i = filaInicio; i <= filaFin && muestra.size() < 10; i++) {
            Row fila = hoja.getRow(i);
            if (fila != null && !esFilaVacia(fila)) {
                muestra.add(leerFilaCompleta(fila, numColumnas));
            }
        }
        return muestra;
    }
}