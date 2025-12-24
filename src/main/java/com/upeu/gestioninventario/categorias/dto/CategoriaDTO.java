package com.upeu.gestioninventario.categorias.dto;

import java.time.LocalDateTime;

public record CategoriaDTO(
        Long id,
        String nombreCategoria,
        String descripcion,
        LocalDateTime fechaCreacion,
        String usuarioCreacionNombre,
        LocalDateTime fechaUltimaModificacion,
        String usuarioUltimaModificacionNombre,
        String icono,
        String color,
        Boolean estado,
        Boolean esInteligente,
        Integer cantidadBienes
) {
}
