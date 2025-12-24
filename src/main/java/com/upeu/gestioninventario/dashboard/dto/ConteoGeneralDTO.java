package com.upeu.gestioninventario.dashboard.dto;

public record ConteoGeneralDTO(
        long totalBienes,
        long totalCategorias,
        long totalEstaciones,
        long totalPersonas,
        long usuariosActivos,
        long usuariosInactivos
) {}
