package com.upeu.gestioninventario.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UsuarioLoginInput(
        @NotBlank(message = "El email del usuario no puede estar vacío")
        String email,

        @NotBlank(message = "La contraseña no puede estar vacía")
        String password,

        boolean rememberMe
) {
}
