package com.upeu.gestioninventario.dashboard.dto;

public record EstructuraResumenDTO(
        long edificios,
        long pisos,
        long ambientes,
        long estaciones,
        long estacionesConBienes,
        long estacionesVacias
) {}
