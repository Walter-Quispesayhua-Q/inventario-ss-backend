package com.upeu.gestioninventario.dashboard.dto;

public record BienesPorCategoriaDTO(
        Long categoriaId,
        String nombreCategoria,
        long cantidad
) {}
