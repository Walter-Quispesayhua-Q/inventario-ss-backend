package com.upeu.gestioninventario.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestInput(
        @NotBlank(message = "El email no puede estar vacío.")
        @Email(message = "Debe proporcionar un formato de email válido.")
        String email
) {
}