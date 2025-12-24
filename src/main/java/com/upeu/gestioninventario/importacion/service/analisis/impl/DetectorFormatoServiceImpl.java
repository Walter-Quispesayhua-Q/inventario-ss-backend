package com.upeu.gestioninventario.importacion.service.analisis.impl;

import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.service.analisis.IDetectorFormato;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.upeu.gestioninventario.importacion.service.analisis.IFormatoConfigLoader;


import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectorFormatoServiceImpl implements IDetectorFormato {

    private final IFormatoConfigLoader formatoConfigLoader;

    @Override
    public Map<Integer, ResultadoDeteccionHoja> detectarFormatoTodasHojas(MultipartFile archivo) throws IOException {
        log.info("Iniciando detección de formato para TODAS las hojas: {}", archivo.getOriginalFilename());

        Map<Integer, ResultadoDeteccionHoja> resultadosPorHoja = new LinkedHashMap<>();

        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            int totalHojas = workbook.getNumberOfSheets();
            log.info("Archivo contiene {} hoja(s)", totalHojas);

            for (int i = 0; i < totalHojas; i++) {
                Sheet hoja = workbook.getSheetAt(i);
                String nombreHoja = hoja.getSheetName();

                log.info("Analizando hoja {}/{}: '{}'", i + 1, totalHojas, nombreHoja);

                ResultadoDeteccionHoja resultado = analizarHoja(hoja, i);
                resultadosPorHoja.put(i, resultado);

                log.info("Hoja '{}' - Formato: {} (score: {})",
                        nombreHoja, resultado.formatoDetectado(), resultado.scoreMaximo());
            }
        }

        log.info("Detección completada para {} hoja(s)", resultadosPorHoja.size());
        return resultadosPorHoja;
    }

    @Override
    public ResultadoDeteccionHoja analizarHoja(Sheet hoja, int indice) {
        String nombreHoja = hoja.getSheetName();

        int scoreTabular = calcularScoreTabular(hoja);
        int scoreClaveValor = calcularScoreClaveValor(hoja);
        int scoreMatriz = calcularScoreFichaMatriz(hoja);

        log.debug("Hoja '{}' - Scores: TABULAR={}, CLAVE_VALOR={}, MATRIZ={}",
                nombreHoja, scoreTabular, scoreClaveValor, scoreMatriz);

        int maxScore = Math.max(scoreTabular, Math.max(scoreClaveValor, scoreMatriz));

        TipoFormatoDetectado formatoDetectado;

        if (maxScore < 30) {
            log.warn("Hoja '{}': Ningún formato alcanzó el umbral mínimo (30 puntos)", nombreHoja);
            formatoDetectado = TipoFormatoDetectado.DESCONOCIDO;
        } else if (scoreTabular == maxScore) {
            formatoDetectado = TipoFormatoDetectado.TABULAR;
        } else if (scoreClaveValor == maxScore) {
            formatoDetectado = TipoFormatoDetectado.CLAVE_VALOR;
        } else {
            formatoDetectado = TipoFormatoDetectado.MATRIZ;
        }

        return new ResultadoDeteccionHoja(
                indice,
                nombreHoja,
                formatoDetectado,
                scoreTabular,
                scoreClaveValor,
                scoreMatriz,
                maxScore
        );
    }

    private int calcularScoreTabular(Sheet hoja) {
        int score = 0;

        Row primeraFila = hoja.getRow(0);
        if (primeraFila != null) {
            int celdasConTexto = 0;
            int totalCeldas = 0;

            for (Cell celda : primeraFila) {
                totalCeldas++;
                if (celda.getCellType() == CellType.STRING && !celda.getStringCellValue().trim().isEmpty()) {
                    celdasConTexto++;
                }
            }

            if (totalCeldas > 0 && (celdasConTexto * 100.0 / totalCeldas) > 70) {
                score += 40;
                log.trace("Primera fila parece headers (+40)");
            }
        }

        int numColumnasEsperadas = primeraFila != null ? primeraFila.getLastCellNum() : 0;
        int filasConsistentes = 0;
        int totalFilasAnalizadas = Math.min(10, hoja.getLastRowNum());

        for (int i = 1; i <= totalFilasAnalizadas; i++) {
            Row fila = hoja.getRow(i);
            if (fila != null && Math.abs(fila.getLastCellNum() - numColumnasEsperadas) <= 2) {
                filasConsistentes++;
            }
        }

        if (totalFilasAnalizadas > 0 && (filasConsistentes * 100.0 / totalFilasAnalizadas) > 80) {
            score += 30;
            log.trace("Estructura columnar consistente (+30)");
        }

        if (numColumnasEsperadas >= 3) {
            score += 20;
            log.trace("Tiene 3+ columnas (+20)");
        }

        return score;
    }

    private int calcularScoreClaveValor(Sheet hoja) {
        int score = 0;

        int filasConDosColumnas = 0;
        int totalFilasConDatos = 0;
        boolean hayFilasVacias = false;

        for (int i = 0; i <= Math.min(hoja.getLastRowNum(), 50); i++) {
            Row fila = hoja.getRow(i);

            if (fila == null || esFilaVacia(fila)) {
                hayFilasVacias = true;
                continue;
            }

            totalFilasConDatos++;

            Cell celdaA = fila.getCell(0);
            Cell celdaB = fila.getCell(1);
            Cell celdaC = fila.getCell(2);

            boolean aEsTexto = celdaA != null && celdaA.getCellType() == CellType.STRING;
            boolean bTieneValor = celdaB != null && !esCeldaVacia(celdaB);
            boolean cVacia = celdaC == null || esCeldaVacia(celdaC);

            if (aEsTexto && bTieneValor && cVacia) {
                filasConDosColumnas++;
            }
        }

        if (totalFilasConDatos > 0 && (filasConDosColumnas * 100.0 / totalFilasConDatos) > 70) {
            score += 50;
            log.trace("Estructura clave-valor dominante (+50)");
        }

        if (hayFilasVacias) {
            score += 20;
            log.trace("Contiene separadores (+20)");
        }

        Row primeraFila = hoja.getRow(0);
        if (primeraFila != null && primeraFila.getLastCellNum() > 4) {
            score -= 30;
            log.trace("Demasiadas columnas (-30)");
        }

        return Math.max(0, score);
    }

    private int calcularScoreFichaMatriz(Sheet hoja) {
        int score = 0;
        int totalFilas = hoja.getLastRowNum();

        var config = formatoConfigLoader.obtenerFormatoMatriz();
        var deteccionFicha = config != null ? config.deteccionFicha() : null;

        if (deteccionFicha == null || !Boolean.TRUE.equals(deteccionFicha.habilitada())) {
            return calcularScoreMatriz(hoja); // Fallback al método original
        }

        int fichasDetectadas = 0;
        List<String> patrones = deteccionFicha.porPatron() != null
                ? deteccionFicha.porPatron().patrones()
                : List.of("^PC-\\d{1,3}$", "^[A-Z]{2,5}-\\d{1,5}$");

        for (int i = 0; i <= totalFilas; i++) {
            Row fila = hoja.getRow(i);
            if (fila == null) continue;

            Cell celdaA = fila.getCell(0);
            if (celdaA != null && celdaA.getCellType() == CellType.STRING) {
                String texto = celdaA.getStringCellValue().trim();

                for (String patron : patrones) {
                    try {
                        if (texto.matches(patron)) {
                            fichasDetectadas++;
                            log.trace("Ficha detectada en fila {}: '{}'", i, texto);
                            break;
                        }
                    } catch (Exception e) {
                        // Ignorar patrones inválidos
                    }
                }
            }
        }

        if (fichasDetectadas >= 2) {
            score += 50;
            log.trace("Detectadas {} fichas (+50)", fichasDetectadas);
        } else if (fichasDetectadas == 1) {
            score += 25;
            log.trace("Detectada 1 ficha (+25)");
        }

        int seccionesClaveValor = contarSeccionesClaveValor(hoja);
        if (seccionesClaveValor >= 2) {
            score += 20;
            log.trace("Detectadas {} secciones clave-valor (+20)", seccionesClaveValor);
        }

        int separadores = contarFilasVaciasComoSeparadores(hoja);
        if (separadores >= 1) {
            score += 15;
            log.trace("Detectados {} separadores (+15)", separadores);
        }

        if (tieneCeldasCombinadasComoTitulos(hoja)) {
            score += 15;
            log.trace("Detectadas celdas combinadas como títulos (+15)");
        }

        return score;
    }

    private int contarSeccionesClaveValor(Sheet hoja) {
        int secciones = 0;
        int filasClaveValorConsecutivas = 0;

        for (int i = 0; i <= hoja.getLastRowNum(); i++) {
            Row fila = hoja.getRow(i);

            if (fila == null || esFilaVacia(fila)) {
                if (filasClaveValorConsecutivas >= 2) {
                    secciones++;
                }
                filasClaveValorConsecutivas = 0;
                continue;
            }

            Cell celdaA = fila.getCell(0);
            Cell celdaB = fila.getCell(1);
            Cell celdaC = fila.getCell(2);

            boolean aEsTexto = celdaA != null && celdaA.getCellType() == CellType.STRING;
            boolean bTieneValor = celdaB != null && !esCeldaVacia(celdaB);
            boolean cVacia = celdaC == null || esCeldaVacia(celdaC);

            if (aEsTexto && bTieneValor && cVacia) {
                filasClaveValorConsecutivas++;
            } else {
                if (filasClaveValorConsecutivas >= 2) {
                    secciones++;
                }
                filasClaveValorConsecutivas = 0;
            }
        }

        return secciones;
    }

    private int contarFilasVaciasComoSeparadores(Sheet hoja) {
        int separadores = 0;
        boolean anteriorTeniaDatos = false;

        for (int i = 0; i <= hoja.getLastRowNum(); i++) {
            Row fila = hoja.getRow(i);
            boolean esVacia = fila == null || esFilaVacia(fila);

            if (esVacia && anteriorTeniaDatos) {
                separadores++;
            }

            anteriorTeniaDatos = !esVacia;
        }

        return separadores;
    }

    private boolean tieneCeldasCombinadasComoTitulos(Sheet hoja) {
        for (var region : hoja.getMergedRegions()) {
            if (region.getNumberOfCells() >= 2) {
                Row fila = hoja.getRow(region.getFirstRow());
                if (fila != null) {
                    Cell celda = fila.getCell(region.getFirstColumn());
                    if (celda != null && celda.getCellType() == CellType.STRING) {
                        String texto = celda.getStringCellValue().trim();
                        if (!texto.isEmpty() && texto.length() < 50) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private int calcularScoreMatriz(Sheet hoja) {
        int score = 0;

        Row primeraFila = hoja.getRow(0);
        if (primeraFila == null) return 0;

        int filasConTextoEnA = 0;
        int totalFilas = Math.min(20, hoja.getLastRowNum());

        for (int i = 1; i <= totalFilas; i++) {
            Row fila = hoja.getRow(i);
            if (fila != null) {
                Cell celdaA = fila.getCell(0);
                if (celdaA != null && celdaA.getCellType() == CellType.STRING) {
                    filasConTextoEnA++;
                }
            }
        }

        if (totalFilas > 0 && (filasConTextoEnA * 100.0 / totalFilas) > 70) {
            score += 35;
            log.trace("Columna A con headers (+35)");
        }

        int headersEnFila1 = 0;
        for (int col = 1; col < primeraFila.getLastCellNum(); col++) {
            Cell celda = primeraFila.getCell(col);
            if (celda != null && celda.getCellType() == CellType.STRING) {
                headersEnFila1++;
            }
        }

        if (headersEnFila1 >= 2) {
            score += 35;
            log.trace("Fila 1 con headers (+35)");
        }

        int celdasNumericas = 0;
        int celdasAnalizadas = 0;

        for (int i = 1; i <= Math.min(10, hoja.getLastRowNum()); i++) {
            Row fila = hoja.getRow(i);
            if (fila != null) {
                for (int col = 1; col < Math.min(10, fila.getLastCellNum()); col++) {
                    Cell celda = fila.getCell(col);
                    celdasAnalizadas++;
                    if (celda != null && celda.getCellType() == CellType.NUMERIC) {
                        celdasNumericas++;
                    }
                }
            }
        }

        if (celdasAnalizadas > 0 && (celdasNumericas * 100.0 / celdasAnalizadas) > 50) {
            score += 30;
            log.trace("Datos principalmente numéricos (+30)");
        }

        return score;
    }

    private boolean esFilaVacia(Row fila) {
        if (fila == null) return true;
        for (Cell celda : fila) {
            if (!esCeldaVacia(celda)) {
                return false;
            }
        }
        return true;
    }

    private boolean esCeldaVacia(Cell celda) {
        if (celda == null) return true;
        if (celda.getCellType() == CellType.BLANK) return true;
        if (celda.getCellType() == CellType.STRING && celda.getStringCellValue().trim().isEmpty()) {
            return true;
        }
        return false;
    }
}
