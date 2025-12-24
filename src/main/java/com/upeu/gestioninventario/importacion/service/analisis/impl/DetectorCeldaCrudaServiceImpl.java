package com.upeu.gestioninventario.importacion.service.analisis.impl;

import com.upeu.gestioninventario.importacion.service.analisis.IDetectorCeldaCruda;
import com.upeu.gestioninventario.ml.service.PlantillaLoaderService;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectorCeldaCrudaServiceImpl implements IDetectorCeldaCruda {

    private final PlantillaLoaderService plantillaLoaderService;

    private static final double UMBRAL_LONGITUD_TEXTO = 30.0;
    private static final double UMBRAL_COLUMNAS_VACIAS = 0.7;
    private static final int MIN_FILAS_MUESTRA = 3;

    @Override
    public boolean esFormatoCeldaCruda(List<String> headers, List<List<String>> filasMuestra) {
        if (headers == null || headers.isEmpty() || filasMuestra == null || filasMuestra.isEmpty()) {
            return false;
        }
        return calcularConfianzaCeldaCruda(headers, filasMuestra) >= 0.7;
    }

    @Override
    public int obtenerColumnaConDatos(List<String> headers, List<List<String>> filasMuestra) {
        if (headers == null || headers.isEmpty() || filasMuestra == null || filasMuestra.isEmpty()) {
            return -1;
        }

        int mejorColumna = -1;
        double mejorScore = 0;

        for (int col = 0; col < headers.size(); col++) {
            double score = calcularScoreColumna(col, filasMuestra);
            if (score > mejorScore) {
                mejorScore = score;
                mejorColumna = col;
            }
        }

        return mejorColumna;
    }

    @Override
    public double calcularConfianzaCeldaCruda(List<String> headers, List<List<String>> filasMuestra) {
        if (headers == null || headers.size() <= 1) {
            return 0.0;
        }

        if (filasMuestra == null || filasMuestra.size() < MIN_FILAS_MUESTRA) {
            return 0.0;
        }

        double score = 0.0;

        // 1. Verificar si hay pocas columnas con datos reales
        double ratioColumnasVacias = calcularRatioColumnasVacias(filasMuestra, headers.size());
        if (ratioColumnasVacias >= UMBRAL_COLUMNAS_VACIAS) {
            score += 0.3;
            log.debug("Ratio columnas vacías alto: {} (+0.3)", ratioColumnasVacias);
        }

        // 2. Verificar si hay una columna dominante con texto largo
        int columnaDominante = obtenerColumnaDominante(filasMuestra, headers.size());
        if (columnaDominante >= 0) {
            double promedioLongitud = calcularPromedioLongitudColumna(columnaDominante, filasMuestra);
            if (promedioLongitud >= UMBRAL_LONGITUD_TEXTO) {
                score += 0.3;
                log.debug("Columna {} tiene texto largo promedio: {} (+0.3)", columnaDominante, promedioLongitud);
            }
        }

        // 3. Verificar presencia de etiquetas conocidas (Ma:, Mo:, Se:, etc.)
        double ratioEtiquetas = calcularRatioEtiquetasDetectadas(filasMuestra);
        if (ratioEtiquetas >= 0.3) {
            score += 0.4;
            log.debug("Ratio de etiquetas detectadas: {} (+0.4)", ratioEtiquetas);
        }

        log.info("Score celda cruda calculado: {}", score);
        return Math.min(1.0, score);
    }

    private double calcularRatioColumnasVacias(List<List<String>> filas, int totalColumnas) {
        if (totalColumnas <= 1) return 0.0;

        int[] conteoNoVacios = new int[totalColumnas];

        for (List<String> fila : filas) {
            for (int col = 0; col < Math.min(fila.size(), totalColumnas); col++) {
                String valor = fila.get(col);
                if (valor != null && !valor.trim().isEmpty()) {
                    conteoNoVacios[col]++;
                }
            }
        }

        int columnasConDatos = 0;
        for (int conteo : conteoNoVacios) {
            if (conteo > filas.size() * 0.3) {
                columnasConDatos++;
            }
        }

        return 1.0 - ((double) columnasConDatos / totalColumnas);
    }

    private int obtenerColumnaDominante(List<List<String>> filas, int totalColumnas) {
        double[] longitudPromedio = new double[totalColumnas];

        for (int col = 0; col < totalColumnas; col++) {
            longitudPromedio[col] = calcularPromedioLongitudColumna(col, filas);
        }

        int columnaDominante = 0;
        double maxLongitud = longitudPromedio[0];
        for (int col = 1; col < totalColumnas; col++) {
            if (longitudPromedio[col] > maxLongitud) {
                maxLongitud = longitudPromedio[col];
                columnaDominante = col;
            }
        }

        return maxLongitud > 10 ? columnaDominante : -1;
    }

    private double calcularPromedioLongitudColumna(int columna, List<List<String>> filas) {
        int suma = 0;
        int count = 0;

        for (List<String> fila : filas) {
            if (columna < fila.size()) {
                String valor = fila.get(columna);
                if (valor != null && !valor.trim().isEmpty()) {
                    suma += valor.trim().length();
                    count++;
                }
            }
        }

        return count > 0 ? (double) suma / count : 0.0;
    }

    private double calcularScoreColumna(int columna, List<List<String>> filas) {
        double longitudPromedio = calcularPromedioLongitudColumna(columna, filas);
        double ratioNoVacios = 0;

        int count = 0;
        for (List<String> fila : filas) {
            if (columna < fila.size()) {
                String valor = fila.get(columna);
                if (valor != null && !valor.trim().isEmpty()) {
                    count++;
                }
            }
        }
        ratioNoVacios = filas.isEmpty() ? 0 : (double) count / filas.size();

        return (longitudPromedio / 100.0) + ratioNoVacios;
    }

    private double calcularRatioEtiquetasDetectadas(List<List<String>> filas) {
        List<Pattern> patronesEtiquetas = obtenerPatronesEtiquetasDePlantillas();

        if (patronesEtiquetas.isEmpty()) {
            patronesEtiquetas = List.of(
                Pattern.compile("(?i)(ma:|marca:)"),
                Pattern.compile("(?i)(mo:|modelo:)"),
                Pattern.compile("(?i)(se:|serie:|serial:)"),
                Pattern.compile("(?i)(co:|color:)"),
                Pattern.compile("(?i)(caf:|cod\\.?\\s*activo:)")
            );
        }

        int filasConEtiquetas = 0;
        for (List<String> fila : filas) {
            String textoFila = String.join(" ", fila);
            for (Pattern patron : patronesEtiquetas) {
                if (patron.matcher(textoFila).find()) {
                    filasConEtiquetas++;
                    break;
                }
            }
        }

        return filas.isEmpty() ? 0 : (double) filasConEtiquetas / filas.size();
    }

    private List<Pattern> obtenerPatronesEtiquetasDePlantillas() {
        List<Pattern> patrones = new ArrayList<>();

        try {
            Collection<PlantillaSeedDTO> plantillas = plantillaLoaderService.getPlantillasCache().values();

            for (PlantillaSeedDTO plantilla : plantillas) {
                if (plantilla.patronesRegex() != null) {
                    Object identificadores = plantilla.patronesRegex().get("identificadores");
                    if (identificadores instanceof Map<?, ?> mapaId) {
                        for (Object valor : mapaId.values()) {
                            if (valor instanceof String patronStr) {
                                try {
                                    patrones.add(Pattern.compile(patronStr, Pattern.CASE_INSENSITIVE));
                                } catch (Exception e) {
                                    log.trace("Patrón inválido ignorado: {}", patronStr);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error obteniendo patrones de plantillas: {}", e.getMessage());
        }

        return patrones;
    }
}
