package com.upeu.gestioninventario.configuracion.dto;

import jakarta.validation.constraints.NotBlank;

public record ParametroSistemaActualizacionDTO(

        @NotBlank(message = "El valor del parámetro no puede estar vacío")
        String valor,

        String descripcion
) {
}