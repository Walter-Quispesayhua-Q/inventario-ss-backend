package com.upeu.gestioninventario.importacion.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public record GrupoConfirmadoInput(
        @NotBlank(message = "El nombre de la categoría no puede estar vacío.")
        String nombreCategoria,
        List<Map<String, Object>> items
) {
}