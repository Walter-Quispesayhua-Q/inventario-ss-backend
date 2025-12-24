package com.upeu.gestioninventario.auth.dto;

import java.util.List;

public record UsuarioLoginResponseDTO(
        Long id,
        String accessToken,
        String refreshToken,
        String email,
        String firstName,
        String lastName,
        String codigoUsuario,
        Boolean activo,
        List<String> roles,
        String departamentoNombre
) {
}
