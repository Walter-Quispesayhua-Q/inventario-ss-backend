package com.upeu.gestioninventario.inventario.dto.bien;

public record AtributoDisplayDTO(
        Long tipoAtributoId,
        String nombreAtributo,
        String valor,
        String tipoDato
) {
}