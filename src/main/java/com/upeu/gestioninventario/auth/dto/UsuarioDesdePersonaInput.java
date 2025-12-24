package com.upeu.gestioninventario.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioDesdePersonaInput(

        @NotNull(message = "El ID de persona es requerido")
        Long idPersona,

        @NotBlank(message = "El email es requerido")
        @Email(message = "El formato del email no es válido")
        String email,

        @NotBlank(message = "La contraseña es requerida")
        String password,

        Long departamentoId,

        String rolInicial
) {}

