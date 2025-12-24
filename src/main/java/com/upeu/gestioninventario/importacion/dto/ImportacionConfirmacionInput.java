package com.upeu.gestioninventario.importacion.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ImportacionConfirmacionInput(
        @NotBlank(message = "El ID de sesión de importación no puede estar vacío.")
        String importacionSesionId,

        List<GrupoConfirmadoInput> grupos
) {
    public boolean tieneGrupos() {
        return grupos != null && !grupos.isEmpty();
    }
}