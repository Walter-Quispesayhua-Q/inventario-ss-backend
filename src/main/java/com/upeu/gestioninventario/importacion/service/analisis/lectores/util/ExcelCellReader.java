package com.upeu.gestioninventario.importacion.service.analisis.lectores.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelCellReader {

    public String leerValorCelda(Cell celda) {
        if (celda == null) return "";

        return switch (celda.getCellType()) {
            case STRING -> celda.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(celda)) {
                    yield celda.getDateCellValue().toString();
                } else {
                    double numValue = celda.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        yield String.valueOf((long) numValue);
                    } else {
                        yield String.valueOf(numValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(celda.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield celda.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(celda.getNumericCellValue());
                }
            }
            case BLANK -> "";
            default -> "";
        };
    }

    public boolean esCeldaVacia(Cell celda) {
        if (celda == null) return true;
        if (celda.getCellType() == CellType.BLANK) return true;
        if (celda.getCellType() == CellType.STRING) {
            return celda.getStringCellValue().trim().isEmpty();
        }
        return false;
    }

    public boolean esFilaVacia(Row fila) {
        if (fila == null) return true;
        for (Cell celda : fila) {
            if (!esCeldaVacia(celda)) {
                return false;
            }
        }
        return true;
    }

    public List<String> leerFilaCompleta(Row fila, int numColumnas) {
        List<String> valores = new ArrayList<>();
        if (fila == null) {
            for (int i = 0; i < numColumnas; i++) {
                valores.add("");
            }
            return valores;
        }

        for (int i = 0; i < numColumnas; i++) {
            Cell celda = fila.getCell(i);
            valores.add(leerValorCelda(celda));
        }
        return valores;
    }

    public String leerCeldaCombinada(Sheet hoja, int fila, int columna) {
        Cell celda = hoja.getRow(fila) != null ? hoja.getRow(fila).getCell(columna) : null;

        for (CellRangeAddress rango : hoja.getMergedRegions()) {
            if (rango.isInRange(fila, columna)) {
                Row primeraFila = hoja.getRow(rango.getFirstRow());
                if (primeraFila != null) {
                    Cell primeraCelda = primeraFila.getCell(rango.getFirstColumn());
                    return leerValorCelda(primeraCelda);
                }
            }
        }

        return leerValorCelda(celda);
    }

    public boolean esCeldaCombinada(Sheet hoja, int fila, int columna) {
        for (CellRangeAddress rango : hoja.getMergedRegions()) {
            if (rango.isInRange(fila, columna)) {
                return true;
            }
        }
        return false;
    }

    public CellRangeAddress obtenerRangoCeldaCombinada(Sheet hoja, int fila, int columna) {
        for (CellRangeAddress rango : hoja.getMergedRegions()) {
            if (rango.isInRange(fila, columna)) {
                return rango;
            }
        }
        return null;
    }

    public String obtenerCoordenada(int fila, int columna) {
        return convertirColumnaALetra(columna) + (fila + 1);
    }

    public String convertirColumnaALetra(int columna) {
        StringBuilder sb = new StringBuilder();
        columna++;
        while (columna > 0) {
            columna--;
            sb.insert(0, (char) ('A' + (columna % 26)));
            columna /= 26;
        }
        return sb.toString();
    }

    public int convertirLetraAColumna(String letra) {
        if (letra == null || letra.isEmpty()) return 0;
        int columna = 0;
        for (char c : letra.toUpperCase().toCharArray()) {
            columna = columna * 26 + (c - 'A' + 1);
        }
        return columna - 1;
    }

    public int detectarNumeroColumnas(Sheet hoja, int maxFilasAEscanear) {
        int maxColumnas = 0;
        int filasEscaneadas = Math.min(maxFilasAEscanear, hoja.getLastRowNum() + 1);

        for (int i = 0; i < filasEscaneadas; i++) {
            Row fila = hoja.getRow(i);
            if (fila != null) {
                maxColumnas = Math.max(maxColumnas, fila.getLastCellNum());
            }
        }

        return maxColumnas;
    }
}
