package com.upeu.gestioninventario.categorias.dto.tipoatributo;

import jakarta.validation.constraints.NotBlank;

public record TipoAtributoCreacionDTO(
        @NotBlank(message = "El nombre del tipo de atributo no puede estar vacío")
        String nombreTipoAtributo,

        @NotBlank(message = "El tipo de dato no puede estar vacío")
        String tipoDato
) {
}
