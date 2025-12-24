package com.upeu.gestioninventario.importacion.dto;

public record ColumnaDetectadaDTO(
        int indice,
        String nombreOriginal,
        String campoMapeado,
        double confianza,
        boolean mapeada
) {
}
