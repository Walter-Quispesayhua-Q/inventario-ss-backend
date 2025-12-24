package com.upeu.gestioninventario.categorias.dto;

import jakarta.validation.constraints.Size;

public record CategoriaActualizacionDTO(
        @Size(min = 3, message = "El nombre de la categoría debe tener al menos 3 caracteres")
        String nombreCategoria,
        String descripcion,
        String icono,
        String color,
        Boolean estado
) {
}
