package com.upeu.gestioninventario.personas.dto;

public record BienResumenDTO(
        Long id,
        String nombreBien,
        String caf,
        String categoriaNombre,
        String ubicacionNombre
) {}
