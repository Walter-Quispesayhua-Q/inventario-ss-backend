package com.upeu.gestioninventario.ubicaciones.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartamentoCreacionDTO(

        @NotBlank(message = "El nombre del departamento no puede estar vacío")
        String nombreDepartamento,

        String descripcion
) {
}