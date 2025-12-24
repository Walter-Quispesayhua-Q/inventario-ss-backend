package com.upeu.gestioninventario.importacion.dto;

import java.util.Map;

public record EstadisticasImportacionDTO(
        int totalFilas,
        int filasValidas,
        int filasConErrores,
        Map<String, Integer> distribucionCategorias,
        double calidadGeneral,
        
        // Estadísticas para formato MATRIZ/FICHA
        Integer totalEstaciones,  // null si no aplica (formato TABULAR/CLAVE_VALOR)
        String tipoConteo         // "FICHAS" | "FILAS"
) {
}
