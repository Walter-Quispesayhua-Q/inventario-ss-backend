package com.upeu.gestioninventario.dashboard.dto;

public record EstacionesPorAmbienteDTO(
        Long ambienteId,
        String nombreAmbiente,
        String nombrePiso,
        String nombreEdificio,
        long cantidadEstaciones
) {}
