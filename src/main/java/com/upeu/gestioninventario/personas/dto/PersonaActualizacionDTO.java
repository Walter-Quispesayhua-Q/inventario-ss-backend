package com.upeu.gestioninventario.personas.dto;

import jakarta.validation.constraints.Email;

public record PersonaActualizacionDTO(

        String nombre,
        String apellido,
        String identificacion,

        @Email(message = "El formato del correo electrónico no es válido")
        String email,

        String telefono
) {
}