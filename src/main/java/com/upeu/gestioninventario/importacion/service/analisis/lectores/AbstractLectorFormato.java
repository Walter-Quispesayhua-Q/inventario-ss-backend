package com.upeu.gestioninventario.importacion.service.analisis.lectores;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.importacion.dto.FilaPreviewDTO;
import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.dto.formato.common.FichasMultiplesDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.EstructuraHojaDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.FormatoGridDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.MetadatosHojaDTO;
import com.upeu.gestioninventario.importacion.service.analisis.IBuscadorAmbiente;
import com.upeu.gestioninventario.importacion.service.analisis.IDetectorCategoria;
import com.upeu.gestioninventario.importacion.service.analisis.IExtractorAtributos;
import com.upeu.gestioninventario.importacion.service.analisis.IMapeadorDatos;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.builder.EstacionBuilder;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.util.ExcelCellReader;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.util.HeaderDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;

@Slf4j
public abstract class AbstractLectorFormato {

    protected final IMapeadorDatos mapeadorDatos;
    protected final IDetectorCategoria detectorCategoria;
    protected final IExtractorAtributos extractorAtributos;
    protected final IBuscadorAmbiente buscadorAmbiente;
    protected final ExcelCellReader cellReader;
    protected final HeaderDetector headerDetector;
    protected final EstacionBuilder estacionBuilder;

    private static final Set<String> CLAVES_UBICACION = Set.of(
            "ubicacion", "ubicación", "ubicacion actual", "ubicación actual",
            "ubicacion_actual", "ubicación_actual", "ambiente", "laboratorio", "lab", "aula",
            "salon", "salón", "dpto", "departamento", "área", "area",
            "area asignada", "área asignada"
    );

    private static final Set<String> CLAVES_RESPONSABLE = Set.of(
            "responsable", "custodio", "encargado", "usuario", "asignado",
            "responsable_actual", "responsable actual", "a cargo de"
    );

    private static final Set<String> CLAVES_METADATOS = Set.of(
            "ubicacion", "ubicación", "ubicacion actual", "ubicación actual",
            "responsable", "custodio", "encargado", "usuario",
            "departamento", "area", "área", "dpto",
            "fecha", "fecha_inventario", "fecha inventario",
            "observaciones", "notas", "comentarios",
            "descripcion del bien", "descripción del bien", "equipo", "nombre equipo"
    );

    protected AbstractLectorFormato(
            IMapeadorDatos mapeadorDatos,
            IDetectorCategoria detectorCategoria,
            IExtractorAtributos extractorAtributos,
            IBuscadorAmbiente buscadorAmbiente,
            ExcelCellReader cellReader,
            HeaderDetector headerDetector,
            EstacionBuilder estacionBuilder) {
        this.mapeadorDatos = mapeadorDatos;
        this.detectorCategoria = detectorCategoria;
        this.extractorAtributos = extractorAtributos;
        this.buscadorAmbiente = buscadorAmbiente;
        this.cellReader = cellReader;
        this.headerDetector = headerDetector;
        this.estacionBuilder = estacionBuilder;
    }

    // ========== DELEGACIÓN A ExcelCellReader ==========

    protected String leerValorCelda(Cell celda) {
        return cellReader.leerValorCelda(celda);
    }

    protected boolean esFilaVacia(Row fila) {
        return cellReader.esFilaVacia(fila);
    }

    protected List<String> leerFilaCompleta(Row fila, int numColumnas) {
        return cellReader.leerFilaCompleta(fila, numColumnas);
    }

    protected String leerCeldaCombinada(Sheet hoja, int fila, int columna) {
        return cellReader.leerCeldaCombinada(hoja, fila, columna);
    }

    protected String obtenerCoordenada(int fila, int columna) {
        return cellReader.obtenerCoordenada(fila, columna);
    }

    protected int convertirLetraAColumna(String letra) {
        return cellReader.convertirLetraAColumna(letra);
    }

    protected int detectarNumeroColumnas(Sheet hoja, int maxFilasAEscanear) {
        return cellReader.detectarNumeroColumnas(hoja, maxFilasAEscanear);
    }

    protected boolean esCeldaCombinada(Sheet hoja, int fila, int columna) {
        return cellReader.esCeldaCombinada(hoja, fila, columna);
    }

    protected CellRangeAddress obtenerRangoCeldaCombinada(Sheet hoja, int fila, int columna) {
        return cellReader.obtenerRangoCeldaCombinada(hoja, fila, columna);
    }

    // ========== DELEGACIÓN A HeaderDetector ==========

    protected int detectarFilaHeaders(Sheet hoja, int maxFilas, double umbralDensidad) {
        return headerDetector.detectarFilaHeaders(hoja, maxFilas, umbralDensidad);
    }


    // ========== DELEGACIÓN A EstacionBuilder ==========

    protected EstacionConComponentesDTO crearEstacionDesdeGrupo(
            String idFicha,
            List<FilaPreviewDTO> bienesGrupo,
            AmbienteDetectadoDTO ambiente,
            int indice) {
        return estacionBuilder.crearEstacionDesdeGrupo(idFicha, bienesGrupo, ambiente, indice);
    }


    protected Map<String, List<FilaPreviewDTO>> agruparPorFichas(List<FilaPreviewDTO> filas, FichasMultiplesDTO config) {
        return estacionBuilder.agruparPorFichas(filas, config);
    }

    // ========== MÉTODOS DE METADATOS ==========

    protected Map<String, String> extraerMetadatosGenericos(Sheet hoja, int filaInicio, int filaFin) {
        Map<String, String> metadatos = new LinkedHashMap<>();

        for (int i = filaInicio; i < filaFin; i++) {
            Row fila = hoja.getRow(i);
            if (fila == null || esFilaVacia(fila)) continue;

            String clave = leerValorCelda(fila.getCell(0));
            String valor = leerValorCelda(fila.getCell(1));

            if (valor.isEmpty() && fila.getLastCellNum() > 2) {
                valor = leerValorCelda(fila.getCell(2));
            }

            if (!clave.isEmpty() && esClaveMetadato(clave)) {
                String claveNormalizada = normalizarClaveMetadato(clave);
                if (!valor.isEmpty()) {
                    metadatos.put(claveNormalizada, valor);
                    log.debug("Metadato extraído: {} = {}", claveNormalizada, valor);
                }
            }
        }

        return metadatos;
    }

    protected boolean esClaveMetadato(String clave) {
        if (clave == null || clave.isEmpty()) return false;
        String claveNormalizada = clave.toLowerCase().trim().replaceAll("[:\\s]+$", "");
        return CLAVES_METADATOS.stream().anyMatch(claveNormalizada::contains);
    }

    protected String normalizarClaveMetadato(String clave) {
        if (clave == null) return "DESCONOCIDO";

        String claveNormalizada = clave.toLowerCase().trim().replaceAll("[:\\s]+$", "");

        if (CLAVES_UBICACION.stream().anyMatch(claveNormalizada::contains)) {
            return "UBICACION";
        }
        if (CLAVES_RESPONSABLE.stream().anyMatch(claveNormalizada::contains)) {
            return "RESPONSABLE";
        }
        if (claveNormalizada.contains("departamento") || claveNormalizada.contains("dpto") || claveNormalizada.contains("area")) {
            return "DEPARTAMENTO";
        }
        if (claveNormalizada.contains("fecha")) {
            return "FECHA";
        }
        if (claveNormalizada.contains("observacion") || claveNormalizada.contains("nota") || claveNormalizada.contains("comentario")) {
            return "OBSERVACIONES";
        }
        if (claveNormalizada.contains("descripcion") || claveNormalizada.contains("equipo") || claveNormalizada.contains("bien")) {
            return "NOMBRE_ESTACION";
        }

        return clave.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
    }

    // ========== CONSTRUCTORES DE DTOs ==========

    protected MetadatosHojaDTO construirMetadatosHoja(
            TipoFormatoDetectado tipoFormato,
            String nombreHoja,
            int indiceHoja,
            int totalFilas,
            int totalColumnas,
            int scoreDeteccion) {

        return MetadatosHojaDTO.crear(tipoFormato, nombreHoja, indiceHoja, totalFilas, totalColumnas, scoreDeteccion);
    }

    protected FormatoGridDTO construirFormatoGrid(
            int filaInicio,
            int filaFin,
            int colInicio,
            int colFin,
            int filaHeaders) {

        String rangoInicio = obtenerCoordenada(filaInicio, colInicio);
        String rangoFin = obtenerCoordenada(filaFin, colFin);

        return FormatoGridDTO.crear(rangoInicio, rangoFin, filaHeaders, colInicio);
    }

    protected EstructuraHojaDTO construirEstructuraTabular(List<String> headers) {
        return EstructuraHojaDTO.paraTabular(headers);
    }

    protected EstructuraHojaDTO construirEstructuraMatriz(
            List<String> headersColumnas,
            List<String> headersFilas,
            List<Map<String, String>> metadatosPorFicha) {

        return EstructuraHojaDTO.paraMatrizConMetadatos(headersColumnas, headersFilas, metadatosPorFicha);
    }

    // ========== UBICACIÓN Y AMBIENTE ==========

    protected String extraerTextoUbicacion(Map<String, String> datos) {
        if (datos == null || datos.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, String> entry : datos.entrySet()) {
            String claveNormalizada = entry.getKey().toLowerCase().trim();

            for (String claveUbicacion : CLAVES_UBICACION) {
                if (claveNormalizada.contains(claveUbicacion)) {
                    String valor = entry.getValue();
                    if (valor != null && !valor.isBlank()) {
                        valor = limpiarPrefijoUbicacion(valor);
                        if (!valor.isBlank()) {
                            log.debug("Texto de ubicación encontrado en clave '{}': '{}'", entry.getKey(), valor);
                            return valor.trim();
                        }
                    }
                }
            }
        }

        return null;
    }

    private String limpiarPrefijoUbicacion(String valor) {
        if (valor == null) return null;
        return valor.replaceFirst("(?i)^(ubicaci[oó]n\\s*(actual)?|dpto\\s*/\\s*[aá]rea|responsable|descripci[oó]n\\s*del\\s*bien)\\s*:\\s*", "").trim();
    }

    protected AmbienteDetectadoDTO buscarAmbientePorUbicacion(Map<String, String> datos) {
        String textoUbicacion = extraerTextoUbicacion(datos);

        if (textoUbicacion == null) {
            return AmbienteDetectadoDTO.noDetectado();
        }

        return buscadorAmbiente.buscarAmbienteDTO(textoUbicacion, 0.80);
    }

    protected String extraerResponsableDeDatos(Map<String, String> datos) {
        if (datos == null || datos.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, String> entry : datos.entrySet()) {
            String clave = entry.getKey().toLowerCase().trim();
            if (CLAVES_RESPONSABLE.contains(clave) ||
                    CLAVES_RESPONSABLE.stream().anyMatch(clave::contains)) {
                String valor = entry.getValue();
                if (valor != null && !valor.isBlank()) {
                    log.debug("Responsable detectado desde datos: {}", valor);
                    return valor.trim();
                }
            }
        }

        return null;
    }

    // ========== CONSTRUCCIÓN DE FILAS ==========

    protected FilaPreviewDTO construirFilaPreview(
            int numeroFila,
            Map<String, String> valoresOriginales,
            Map<String, String> atributosExtraidos,
            String categoriaDetectada,
            double confianza,
            AmbienteDetectadoDTO ambienteDetectado) {

        return new FilaPreviewDTO(
                numeroFila,
                valoresOriginales,
                atributosExtraidos,
                true,
                List.of(),
                categoriaDetectada,
                confianza,
                ambienteDetectado,
                null,
                null,
                null,
                List.of(),
                null,
                null
        );
    }

    protected FilaPreviewDTO construirFilaPreview(
            int numeroFila,
            Map<String, String> valoresOriginales,
            Map<String, String> atributosExtraidos,
            String categoriaDetectada,
            double confianza) {

        AmbienteDetectadoDTO ambiente = buscarAmbientePorUbicacion(valoresOriginales);

        if (!ambiente.tieneAmbiente() && ambiente.textoOriginalUbicacion() == null) {
            ambiente = buscarAmbientePorUbicacion(atributosExtraidos);
        }

        return construirFilaPreview(
                numeroFila,
                valoresOriginales,
                atributosExtraidos,
                categoriaDetectada,
                confianza,
                ambiente
        );
    }
}