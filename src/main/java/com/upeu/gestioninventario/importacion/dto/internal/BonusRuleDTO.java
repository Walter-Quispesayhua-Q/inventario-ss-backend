package com.upeu.gestioninventario.importacion.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BonusRuleDTO(
    String nombre,
    String condicion,
    double puntos,
    String descripcion
) {}