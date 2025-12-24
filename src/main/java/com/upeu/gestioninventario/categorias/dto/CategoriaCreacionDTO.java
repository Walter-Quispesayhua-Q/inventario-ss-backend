package com.upeu.gestioninventario.categorias.dto;

import jakarta.validation.constraints.NotBlank;


public record CategoriaCreacionDTO(

        @NotBlank(message = "El nombre de la categoría no puede estar vacío")
        String nombreCategoria,

        String descripcion,
        String icono,
        String color,
        Boolean estado
) {
}
