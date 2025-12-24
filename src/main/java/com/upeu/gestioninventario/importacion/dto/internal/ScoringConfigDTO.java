package com.upeu.gestioninventario.importacion.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScoringConfigDTO(
    Map<String, Double> pesos,
    Map<String, Double> umbrales,
    List<BonificacionDTO> bonificaciones
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BonificacionDTO(
        String nombre,
        String condicion,
        Double puntos,
        String descripcion
    ) {}
}