package com.upeu.gestioninventario.categorias.dto;

import com.upeu.gestioninventario.categorias.dto.tipoatributo.TipoAtributoDTO;

public record CategoriaAtributoDTO(
        Long id,
        String nombreCategoria,
        String descripcion,
        TipoAtributoDTO tipoAtributo,
        boolean activo
) {
}
