package com.upeu.gestioninventario.importacion.service.analisis.lectores;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.estructuras.dto.tipo.TipoComponenteEnum;
import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.importacion.dto.ColumnaDetectadaDTO;
import com.upeu.gestioninventario.importacion.dto.FilaPreviewDTO;
import com.upeu.gestioninventario.importacion.dto.ResultadoDeteccionCategoria;
import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.dto.formato.matriz.EstructuraFichaDTO;
import com.upeu.gestioninventario.importacion.dto.formato.matriz.FormatoMatrizDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.EstructuraHojaDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.FormatoGridDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.HojaAnalizadaDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.MetadatosHojaDTO;
import com.upeu.gestioninventario.importacion.service.analisis.IBuscadorAmbiente;
import com.upeu.gestioninventario.importacion.service.analisis.IDetectorCategoria;
import com.upeu.gestioninventario.importacion.service.analisis.IExtractorAtributos;
import com.upeu.gestioninventario.importacion.service.analisis.IMapeadorDatos;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.builder.EstacionBuilder;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.util.ExcelCellReader;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.util.HeaderDetector;
import com.upeu.gestioninventario.ml.service.PlantillaLoaderService;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class LectorMatrizService extends AbstractLectorFormato implements ILectorFormato<FormatoMatrizDTO> {

    private final PlantillaLoaderService plantillaLoaderService;

    public LectorMatrizService(
            IMapeadorDatos mapeadorDatos,
            IDetectorCategoria detectorCategoria,
            IExtractorAtributos extractorAtributos,
            IBuscadorAmbiente buscadorAmbiente,
            ExcelCellReader cellReader,
            HeaderDetector headerDetector,
            EstacionBuilder estacionBuilder,
            PlantillaLoaderService plantillaLoaderService) {
        super(mapeadorDatos, detectorCategoria, extractorAtributos, buscadorAmbiente, cellReader, headerDetector, estacionBuilder);
        this.plantillaLoaderService = plantillaLoaderService;
    }

    @Override
    public HojaAnalizadaDTO extraerDatos(Sheet hoja, FormatoMatrizDTO config, int indiceHoja) {
        log.info("Extrayendo datos MATRIZ de hoja: {}", hoja.getSheetName());

        List<FilaPreviewDTO> filasEnriquecidas = new ArrayList<>();
        List<EstacionConComponentesDTO> estacionesSugeridas = new ArrayList<>();
        List<HojaAnalizadaDTO.FichaResumenDTO> fichasDetectadas = new ArrayList<>();
        List<List<String>> datosMatriz = new ArrayList<>();
        List<String> headersFilas = new ArrayList<>();
        List<Map<String, String>> metadatosPorFicha = new ArrayList<>();

        List<SeccionFicha> secciones = dividirEnFichas(hoja);
        log.info("Se detectaron {} fichas en la hoja", secciones.size());

        int numeroFilaGlobal = 1;
        int indiceFicha = 1;
        List<String> headersColumnas = new ArrayList<>();

        for (SeccionFicha seccion : secciones) {
            log.debug("Procesando ficha {} (filas {} a {})", indiceFicha, seccion.filaInicio + 1, seccion.filaFin + 1);

            ResultadoProcesamientoFicha resultado = procesarFicha(hoja, config, seccion, numeroFilaGlobal);

            filasEnriquecidas.addAll(resultado.filas);
            datosMatriz.addAll(resultado.datosFilas);
            numeroFilaGlobal += resultado.filas.size();

            if (headersColumnas.isEmpty() && !resultado.headers.isEmpty()) {
                headersColumnas = resultado.headers;
            }

            if (!resultado.filas.isEmpty()) {
                String idFicha = resultado.idFicha != null ? resultado.idFicha : "FICHA-" + indiceFicha;
                headersFilas.add(idFicha);
                metadatosPorFicha.add(resultado.metadatos);

                fichasDetectadas.add(new HojaAnalizadaDTO.FichaResumenDTO(
                        idFicha,
                        seccion.filaInicio,
                        seccion.filaFin,
                        resultado.filas.size(),
                        resultado.metadatos,
                        resultado.filas.isEmpty() ? null : resultado.filas.get(0).categoriaDetectada()
                ));

                AmbienteDetectadoDTO ambiente = resultado.filas.get(0).ambienteDetectado();
                String responsableDetectado = extraerResponsableDeMetadatos(resultado.metadatos);

                EstacionConComponentesDTO estacion = crearEstacionDesdeGrupo(idFicha, resultado.filas, ambiente, indiceFicha);

                if (estacion != null) {
                    estacion.setResponsableDetectado(responsableDetectado);

                    String nombreEstacion = resultado.metadatos.get("NOMBRE_ESTACION");
                    if (nombreEstacion != null && !nombreEstacion.isBlank()) {
                        estacion.setNombre(nombreEstacion.trim());
                    }

                    String codigoDetectado = extraerCodigoEstacionDeMetadatos(resultado.metadatos);
                    if (codigoDetectado != null) {
                        estacion.setCodigo(codigoDetectado);
                        log.debug("Código de estación detectado en metadatos: {}", codigoDetectado);
                    }

                    estacionesSugeridas.add(estacion);
                }
            }

            indiceFicha++;
        }

        log.info("Se procesaron {} filas totales y {} estaciones", filasEnriquecidas.size(), estacionesSugeridas.size());

        MetadatosHojaDTO metadatos = construirMetadatosHoja(
                TipoFormatoDetectado.MATRIZ,
                hoja.getSheetName(),
                indiceHoja,
                filasEnriquecidas.size(),
                headersColumnas.size(),
                80
        );

        EstructuraHojaDTO estructura = construirEstructuraMatriz(headersColumnas, headersFilas, metadatosPorFicha);

        int filaInicio = secciones.isEmpty() ? 0 : secciones.get(0).filaInicio;
        int filaFin = secciones.isEmpty() ? 0 : secciones.get(secciones.size() - 1).filaFin;
        FormatoGridDTO formatoGrid = FormatoGridDTO.paraMatriz(
                obtenerCoordenada(filaInicio, 0),
                obtenerCoordenada(filaFin, headersColumnas.size() - 1),
                -1,
                0
        );

        return HojaAnalizadaDTO.paraMatriz(
                metadatos,
                estructura,
                formatoGrid,
                datosMatriz,
                filasEnriquecidas,
                estacionesSugeridas,
                fichasDetectadas
        );
    }

    private record SeccionFicha(int filaInicio, int filaFin) {}

    private record ResultadoProcesamientoFicha(
            List<FilaPreviewDTO> filas,
            List<List<String>> datosFilas,
            List<String> headers,
            String idFicha,
            Map<String, String> metadatos
    ) {}

    private String extraerCodigoEstacionDeMetadatos(Map<String, String> metadatos) {
        if (metadatos == null || metadatos.isEmpty()) {
            return null;
        }

        String patronCodigo = "(?i)^(PC|FICHA|EST(ACION)?|EQUIPO|EQ|COMPUTADOR[AE]?|COMP|LAP(TOP)?)-?\\d{1,3}$";

        for (String valor : metadatos.values()) {
            if (valor != null && valor.trim().matches(patronCodigo)) {
                return valor.trim();
            }
        }

        String[] clavesEspecificas = {"CODIGO_ESTACION", "CODIGO DE ESTACION", "COD_ESTACION"};
        for (String clave : clavesEspecificas) {
            String valor = metadatos.get(clave);
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }

        return null;
    }

    private List<SeccionFicha> dividirEnFichas(Sheet hoja) {
        List<SeccionFicha> secciones = new ArrayList<>();
        int filaInicio = -1;

        for (int i = 0; i <= hoja.getLastRowNum(); i++) {
            Row fila = hoja.getRow(i);
            boolean esVacia = fila == null || esFilaVacia(fila);

            if (!esVacia && filaInicio == -1) {
                filaInicio = i;
            } else if (esVacia && filaInicio != -1) {
                secciones.add(new SeccionFicha(filaInicio, i - 1));
                filaInicio = -1;
            }
        }

        if (filaInicio != -1) {
            secciones.add(new SeccionFicha(filaInicio, hoja.getLastRowNum()));
        }

        return secciones;
    }

    private ResultadoProcesamientoFicha procesarFicha(
            Sheet hoja,
            FormatoMatrizDTO config,
            SeccionFicha seccion,
            int numeroFilaInicial) {

        List<FilaPreviewDTO> filas = new ArrayList<>();
        List<List<String>> datosFilas = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        String idFicha = null;

        int filaHeaders = detectarFilaHeadersEnSeccion(hoja, seccion.filaInicio, seccion.filaFin);

        if (filaHeaders == -1) {
            log.warn("No se encontraron headers en la sección {}-{}", seccion.filaInicio, seccion.filaFin);
            return new ResultadoProcesamientoFicha(filas, datosFilas, headers, null, Map.of());
        }

        Map<String, String> metadatosFicha = extraerMetadatosDeSeccion(hoja, config, seccion.filaInicio, filaHeaders);

        for (String valor : metadatosFicha.values()) {
            if (valor.matches("(?i)PC-?\\d+.*")) {
                idFicha = valor.trim();
                break;
            }
        }

        Row rowHeaders = hoja.getRow(filaHeaders);
        List<ColumnaDetectadaDTO> columnasMapeadas = new ArrayList<>();
        int numColumnas = 0;
        int columnaNumero = -1;

        if (rowHeaders != null) {
            numColumnas = rowHeaders.getLastCellNum();
            for (int col = 0; col < numColumnas; col++) {
                String header = leerValorCelda(rowHeaders.getCell(col));
                headers.add(header);
                // Detectar columna "Nº" o similar
                if (header.equalsIgnoreCase("Nº") || header.equalsIgnoreCase("N°") || 
                    header.equalsIgnoreCase("No") || header.equalsIgnoreCase("NUM") ||
                    header.equalsIgnoreCase("ITEM") || header.equalsIgnoreCase("#")) {
                    columnaNumero = col;
                }
            }
            columnasMapeadas = mapeadorDatos.mapearNombres(headers);
        }

        if (columnaNumero == -1) {
            columnaNumero = 0;
        }

        int numeroFila = numeroFilaInicial;
        FilaPreviewDTO elementoPadreActual = null;
        int indicePadre = -1;

        for (int i = filaHeaders + 1; i <= seccion.filaFin; i++) {
            Row fila = hoja.getRow(i);
            
            String bienValue = "";
            if (fila != null && fila.getCell(1) != null) {
                bienValue = leerValorCelda(fila.getCell(1));
            }
            
            if (fila == null) {
                log.debug("Fila {} saltada: fila es null", i);
                continue;
            }
            if (esFilaVacia(fila)) {
                log.debug("Fila {} saltada: esFilaVacia=true (BIEN='{}')", i, bienValue);
                continue;
            }

            if (esFilaDeHeader(fila, headers)) {
                log.debug("Fila {} saltada: esFilaDeHeader=true (BIEN='{}')", i, bienValue);
                continue;
            }
            
            log.debug("Procesando fila Excel {}: BIEN='{}'", i, bienValue);

            Map<String, String> valoresOriginales = new LinkedHashMap<>(metadatosFicha);
            List<String> valoresFila = new ArrayList<>();

            for (int col = 0; col < Math.min(headers.size(), numColumnas); col++) {
                String valor = leerCeldaCombinada(hoja, i, col);
                valoresFila.add(valor);

                String headerOriginal = headers.get(col);
                String campoMapeado = columnasMapeadas.size() > col && columnasMapeadas.get(col).mapeada()
                        ? columnasMapeadas.get(col).campoMapeado()
                        : headerOriginal;

                valoresOriginales.put(campoMapeado, valor);
            }

            datosFilas.add(valoresFila);

            // Detectar si es un componente interno (sub-componente)
            boolean esComponenteInterno = esComponenteInternoDePC(hoja, i, columnaNumero, valoresOriginales);
            
            log.debug("Fila Excel {}: esComponenteInterno={}, BIEN={}, elementoPadreActual={}", 
                    i, esComponenteInterno, 
                    valoresOriginales.getOrDefault("BIEN", valoresOriginales.getOrDefault("bien", "(no encontrado)")),
                    elementoPadreActual != null ? elementoPadreActual.numeroFila() : "null");

            if (esComponenteInterno && elementoPadreActual != null) {
                String textoCompleto = String.join(" ", valoresOriginales.values());
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
                        atributosExtraidos = extractorAtributos.extraerAtributos(textoCompleto, valoresOriginales, plantilla);
                    }
                }

                String nombreBienDirecto = obtenerNombreBienDeColumna(valoresOriginales);
                if (nombreBienDirecto != null && !nombreBienDirecto.isBlank()) {
                    atributosExtraidos.put("NOMBRE_BIEN", nombreBienDirecto.trim());
                }
                
                String grupoDelPadre = elementoPadreActual.atributosExtraidos() != null 
                        ? elementoPadreActual.atributosExtraidos().getOrDefault("GRUPO_ESTACION", "1")
                        : "1";
                atributosExtraidos.put("GRUPO_ESTACION", grupoDelPadre);
                
                FilaPreviewDTO filaComponente = construirFilaPreview(
                        numeroFila++, valoresOriginales, atributosExtraidos, categoriaDetectada, confianza);
                
                filaComponente = filaComponente.conEstacion(
                        elementoPadreActual.idEstacionAsociada(),
                        String.valueOf(elementoPadreActual.numeroFila()),
                        TipoComponenteEnum.EQUIPO_COMPLETO);
                
                filas.add(filaComponente);
                log.debug("Componente '{}' creado como fila independiente con categoría '{}'", 
                        nombreBienDirecto, categoriaDetectada);
            } else {
                String textoCompleto = String.join(" ", valoresOriginales.values());
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
                        atributosExtraidos = extractorAtributos.extraerAtributos(textoCompleto, valoresOriginales, plantilla);
                    }
                }

                String nombreBienDirecto = obtenerNombreBienDeColumna(valoresOriginales);
                if (nombreBienDirecto != null && !nombreBienDirecto.isBlank()) {
                    atributosExtraidos.put("NOMBRE_BIEN", nombreBienDirecto.trim());
                }
                
                String numeroGrupo = valoresOriginales.getOrDefault("Nº", valoresOriginales.getOrDefault("N°", ""));
                atributosExtraidos.put("GRUPO_ESTACION", numeroGrupo);

                FilaPreviewDTO nuevaFila = construirFilaPreview(numeroFila++, valoresOriginales, atributosExtraidos, categoriaDetectada, confianza);
                filas.add(nuevaFila);
                
                if (esComponentePrincipalDePC(valoresOriginales)) {
                    elementoPadreActual = nuevaFila;
                    indicePadre = filas.size() - 1;
                } else {
                    elementoPadreActual = null;
                    indicePadre = -1;
                }
            }
        }

        return new ResultadoProcesamientoFicha(filas, datosFilas, headers, idFicha, metadatosFicha);
    }

    /**
     * Determina si una fila es un componente interno de una PC
     * basándose en:
     * 1. Si la columna Nº está vacía o es celda combinada con la fila anterior
     * 2. Si el tipo de bien es un componente interno conocido
     */
    private boolean esComponenteInternoDePC(Sheet hoja, int filaActual, int columnaNumero, Map<String, String> valores) {
        // Verificar si la celda Nº está vacía en esta fila
        Row row = hoja.getRow(filaActual);
        if (row == null) return false;

        Cell celdaNumero = row.getCell(columnaNumero);
        String valorNumero = leerValorCelda(celdaNumero);
        
        log.trace("esComponenteInternoDePC: fila={}, columnaNumero={}, valorNumero='{}', esCeldaCombinada={}", 
                filaActual, columnaNumero, valorNumero, esCeldaCombinada(hoja, filaActual, columnaNumero));
        
        // Si la celda tiene un número propio (1, 2, 3...), NO es componente interno
        if (!valorNumero.isEmpty() && valorNumero.matches("\\d+")) {
            if (esCeldaCombinada(hoja, filaActual, columnaNumero)) {
                CellRangeAddress rango = obtenerRangoCeldaCombinada(hoja, filaActual, columnaNumero);
                if (rango != null && filaActual > rango.getFirstRow()) {
                    log.debug("Fila {} detectada como subcomponente por celda combinada (rango: {})", filaActual, rango);
                    return true;
                }
            }
            return false;
        }

        // Si la celda Nº está vacía, verificar si el tipo de bien es componente interno
        String tipoBien = valores.getOrDefault("BIEN", valores.getOrDefault("bien", ""));
        boolean esInterno = esNombreDeComponenteInterno(tipoBien);
        log.trace("esComponenteInternoDePC: fila={}, tipoBien='{}', esInterno={}", filaActual, tipoBien, esInterno);
        return esInterno;
    }

    /**
     * Verifica si el nombre del bien corresponde a un componente interno
     */
    private boolean esNombreDeComponenteInterno(String nombreBien) {
        if (nombreBien == null || nombreBien.isBlank()) return false;
        
        String nombreNormalizado = nombreBien.toLowerCase().trim();
        
        Set<String> componentesInternos = Set.of(
                "procesador", "cpu", "processor",
                "ssd", "disco duro", "hdd", "nvme", "almacenamiento",
                "memoria ram", "ram", "memoria", "dimm",
                "tarjeta de video", "t. video", "t.video", "gpu", "video", "gráfica", "grafica",
                "tarjeta madre", "motherboard", "fuente", "fuente de poder", "psu"
        );
        
        for (String componente : componentesInternos) {
            if (nombreNormalizado.contains(componente)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Verifica si el bien es un componente principal que puede tener subcomponentes
     */
    private boolean esComponentePrincipalDePC(Map<String, String> valores) {
        String tipoBien = valores.getOrDefault("BIEN", valores.getOrDefault("bien", ""));
        if (tipoBien == null || tipoBien.isBlank()) return false;
        
        String nombreNormalizado = tipoBien.toLowerCase().trim();
        
        Set<String> componentesPrincipales = Set.of(
                "mainboard", "motherboard", "placa madre", "placa base",
                "cpu", "computador", "computadora", "pc", "desktop",
                "laptop", "notebook", "servidor", "workstation"
        );
        
        for (String principal : componentesPrincipales) {
            if (nombreNormalizado.contains(principal)) {
                return true;
            }
        }
        
        return false;
    }

    private String obtenerNombreBienDeColumna(Map<String, String> valoresOriginales) {
        if (valoresOriginales == null || valoresOriginales.isEmpty()) {
            return null;
        }

        List<String> clavesBien = List.of("BIEN", "Bien", "bien", "NOMBRE", "Nombre", "nombre",
                "DESCRIPCION", "Descripcion", "descripcion", "EQUIPO", "Equipo", "equipo");

        for (String clave : clavesBien) {
            String valor = valoresOriginales.get(clave);
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }

        return null;
    }

    private int detectarFilaHeadersEnSeccion(Sheet hoja, int filaInicio, int filaFin) {
        int minimoCoincidencias = 2;

        for (int i = filaInicio; i <= filaFin; i++) {
            Row fila = hoja.getRow(i);
            if (fila == null || esFilaVacia(fila)) continue;

            int camposConocidos = 0;
            int celdasConTexto = 0;

            for (int col = 0; col < fila.getLastCellNum(); col++) {
                String valor = leerValorCelda(fila.getCell(col));
                if (!valor.isEmpty()) {
                    celdasConTexto++;
                    if (mapeadorDatos.esCampoConocido(valor)) {
                        camposConocidos++;
                    }
                }
            }

            if (camposConocidos >= minimoCoincidencias && celdasConTexto >= 3) {
                log.debug("Fila {} detectada como headers: {} campos conocidos", i + 1, camposConocidos);
                return i;
            }
        }

        return -1;
    }

    private Map<String, String> extraerMetadatosDeSeccion(
            Sheet hoja,
            FormatoMatrizDTO config,
            int filaInicio,
            int filaHeaders) {

        Map<String, String> metadatos = new LinkedHashMap<>();

        detectarCodigoEstacionEnSeccion(hoja, filaInicio, filaHeaders, metadatos);

        var estructuraFicha = config.estructuraFicha();
        if (estructuraFicha == null || estructuraFicha.metadatos() == null) {
            Map<String, String> metadatosGenericos = extraerMetadatosGenericos(hoja, filaInicio, filaHeaders);
            metadatos.putAll(metadatosGenericos);
            return metadatos;
        }

        var metadatosConfig = estructuraFicha.metadatos();
        List<String> columnasClave = metadatosConfig.columnasClave();
        List<String> columnasValor = metadatosConfig.columnasValor();
        List<EstructuraFichaDTO.CampoEsperadoDTO> camposEsperados = metadatosConfig.camposEsperados();

        if (camposEsperados == null || camposEsperados.isEmpty()) {
            Map<String, String> metadatosGenericos = extraerMetadatosGenericos(hoja, filaInicio, filaHeaders);
            metadatos.putAll(metadatosGenericos);
            return metadatos;
        }

        for (int i = filaInicio; i < filaHeaders; i++) {
            Row fila = hoja.getRow(i);
            if (fila == null) continue;

            String clave = null;
            for (String colLetra : columnasClave) {
                int colClaveIndex = convertirLetraAColumna(colLetra);
                clave = leerValorCelda(fila.getCell(colClaveIndex));
                if (clave != null && !clave.isEmpty()) break;
            }

            if (clave == null || clave.isEmpty()) continue;

            EstructuraFichaDTO.CampoEsperadoDTO campoEncontrado = buscarCampoEsperado(clave, camposEsperados);

            if (campoEncontrado != null) {
                String valor = buscarValorEnColumnas(fila, columnasValor);

                if (valor != null && !valor.isEmpty()) {
                    metadatos.put(campoEncontrado.extraerPara(), valor);
                    log.debug("Metadato extraído: {} = {}", campoEncontrado.extraerPara(), valor);
                }
            }
        }

        log.info("Metadatos extraídos: {}", metadatos);
        return metadatos;
    }

    private void detectarCodigoEstacionEnSeccion(Sheet hoja, int filaInicio, int filaHeaders, Map<String, String> metadatos) {
        String patronCodigo = "(?i)^(PC|FICHA|EST(ACION)?|EQUIPO|EQ|COMPUTADOR[AE]?|COMP|LAP(TOP)?)-?\\d{1,3}$";
        
        for (int i = filaInicio; i < Math.min(filaInicio + 3, filaHeaders); i++) {
            Row fila = hoja.getRow(i);
            if (fila == null) continue;
            
            for (int col = 0; col <= Math.min(fila.getLastCellNum(), 2); col++) {
                String valor = leerValorCelda(fila.getCell(col));
                if (valor != null && valor.trim().matches(patronCodigo)) {
                    metadatos.put("CODIGO_ESTACION", valor.trim());
                    log.debug("Código de estación detectado en fila {}: {}", i + 1, valor.trim());
                    return;
                }
            }
        }
    }

    private String buscarValorEnColumnas(Row fila, List<String> columnasValor) {
        for (String colLetra : columnasValor) {
            int colIndex = convertirLetraAColumna(colLetra);
            String valor = leerValorCelda(fila.getCell(colIndex));
            if (valor != null && !valor.isEmpty()) {
                return valor;
            }
        }
        return null;
    }

    private EstructuraFichaDTO.CampoEsperadoDTO buscarCampoEsperado(
            String clave,
            List<EstructuraFichaDTO.CampoEsperadoDTO> camposEsperados) {

        if (clave == null || camposEsperados == null) return null;

        String claveNormalizada = clave.toLowerCase().trim().replaceAll("[:\\s]+$", "");

        for (EstructuraFichaDTO.CampoEsperadoDTO campo : camposEsperados) {
            if (campo.clave() != null && campo.clave().equalsIgnoreCase(claveNormalizada)) {
                return campo;
            }

            if (campo.alias() != null &&
                    campo.alias().stream().anyMatch(a -> a.equalsIgnoreCase(claveNormalizada))) {
                return campo;
            }
        }
        return null;
    }

    private boolean esFilaDeHeader(Row fila, List<String> headersOriginales) {
        if (fila == null || headersOriginales.isEmpty()) return false;

        // Verificar si la columna BIEN (típicamente columna 1 o 2) contiene un componente interno
        // Estas filas NO son headers, son datos de componentes internos de PC
        for (int col = 1; col <= 2 && col < fila.getLastCellNum(); col++) {
            String valorBien = leerValorCelda(fila.getCell(col));
            if (esNombreDeComponenteInterno(valorBien)) {
                return false;
            }
        }

        int coincidencias = 0;
        for (int col = 0; col < Math.min(5, fila.getLastCellNum()); col++) {
            String valor = leerValorCelda(fila.getCell(col));
            if (headersOriginales.contains(valor)) {
                coincidencias++;
            }
        }
        return coincidencias >= 2;
    }

    private String extraerResponsableDeMetadatos(Map<String, String> metadatos) {
        if (metadatos == null || metadatos.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, String> entry : metadatos.entrySet()) {
            String clave = entry.getKey().toLowerCase();
            if (clave.contains("responsable") || clave.contains("custodio") ||
                    clave.contains("encargado") || clave.contains("usuario")) {
                String valor = entry.getValue();
                if (valor != null && !valor.isBlank()) {
                    log.debug("Responsable detectado: {}", valor);
                    return valor.trim();
                }
            }
        }

        return null;
    }

    @Override
    public boolean soportaFormato(FormatoMatrizDTO config) {
        return config != null && ("MATRIZ".equals(config.tipoFormato()) || "FICHA".equals(config.tipoFormato()));
    }
}