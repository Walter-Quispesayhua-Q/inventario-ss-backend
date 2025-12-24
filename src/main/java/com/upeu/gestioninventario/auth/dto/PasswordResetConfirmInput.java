package com.upeu.gestioninventario.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetConfirmInput(
        @NotBlank(message = "El token no puede estar vacío")
        String token,
        @NotBlank(message = "La contraseña no puede estar vacía")
        String nuevaPassword
) {
}
