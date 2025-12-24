package com.upeu.gestioninventario.categorias.dto;

public record CategoriaSimpleDTO(
        Long id,
        String nombreCategoria,
        Boolean esInteligente
) {
}
