package com.upeu.gestioninventario.personas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PersonaCreacionDTO(

        @NotBlank(message = "El nombre no puede estar vacío")
        String nombre,

        @NotBlank(message = "El apellido no puede estar vacío")
        String apellido,

        String identificacion,

        @NotBlank(message = "El código no puede estar vacío")
        String codigo,

        @Email(message = "El formato del correo electrónico no es válido")
        String email,

        String telefono
) {
}