package com.upeu.gestioninventario.configuracion.dto;

import jakarta.validation.constraints.NotBlank;

public record ParametroSistemaCreacionDTO(

        @NotBlank(message = "La clave del parámetro no puede estar vacía")
        String clave,

        @NotBlank(message = "El valor del parámetro no puede estar vacío")
        String valor,

        String descripcion
) {
}