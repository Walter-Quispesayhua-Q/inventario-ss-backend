package com.upeu.gestioninventario.auth.dto;

import java.time.LocalDateTime;


public record RolDTO(
        Long id,
        String nombreRol,
        String descripcion,
        LocalDateTime fechaCreacion
) {
}
