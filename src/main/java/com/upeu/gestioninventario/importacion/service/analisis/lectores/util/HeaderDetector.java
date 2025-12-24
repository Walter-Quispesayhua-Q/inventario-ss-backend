package com.upeu.gestioninventario.importacion.service.analisis.lectores.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeaderDetector {

    public int detectarFilaHeaders(Sheet hoja, int maxFilas, double umbralDensidad) {
        for (int i = 0; i < Math.min(maxFilas, hoja.getLastRowNum() + 1); i++) {
            Row fila = hoja.getRow(i);
            if (fila != null && esFilaHeaders(fila, umbralDensidad)) {
                return i;
            }
        }
        return 0;
    }

    public boolean esFilaHeaders(Row fila, double umbralDensidad) {
        if (fila == null) return false;

        int celdasConTexto = 0;
        int totalCeldas = 0;

        for (Cell celda : fila) {
            totalCeldas++;
            if (celda.getCellType() == CellType.STRING && !celda.getStringCellValue().trim().isEmpty()) {
                celdasConTexto++;
            }
        }

        return totalCeldas > 0 && (celdasConTexto * 1.0 / totalCeldas) >= umbralDensidad;
    }
}
