package com.upeu.gestioninventario.auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record RolCreacionDTO(
        Long id,
        @NotBlank(message = "El nombre del rol no puede estar vacío")
        String nombreRol,
        String descripcion,
        LocalDateTime fechaCreacion
) {
}
