package com.upeu.gestioninventario.auth.dto;

import java.time.LocalDateTime;

public record PermisoDTO(
        Long id,
        String nombrePermiso,
        String descripcion,
        LocalDateTime fechaCreacion
) {
}
