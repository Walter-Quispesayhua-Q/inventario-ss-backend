package com.upeu.gestioninventario.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UsuarioCreacionInput(

        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        String firstName,

        @NotBlank(message = "El apellido de usuario no puede estar vacío")
        String lastName,

        @NotBlank(message = "El correo electrónico no puede estar vacío")
        String email,

        @NotBlank(message = "La contraseña no puede estar vacía")
        String password,

        @NotBlank(message = "el departamento no puede estar vacio")
        Long departamentoId
) {
}
