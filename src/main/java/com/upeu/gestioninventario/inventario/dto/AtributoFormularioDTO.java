package com.upeu.gestioninventario.inventario.dto;

import java.util.List;

public record AtributoFormularioDTO(
        String tipoAtributoId,
        String nombreTipoAtributo,
        String etiqueta,
        String tipoDato,
        boolean requerido,
        String valorDefecto,
        int orden,
        String placeholder,
        List<String> opciones
) {
}