package com.upeu.gestioninventario.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UsuarioActualizacionDTO(
        String nombreUsuario,
        String email,
        String codigoUsuario,
        Boolean activo,
        Integer idPersona
) {
}
