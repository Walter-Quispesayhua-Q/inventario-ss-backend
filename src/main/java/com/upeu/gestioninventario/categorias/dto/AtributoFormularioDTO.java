package com.upeu.gestioninventario.categorias.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record AtributoFormularioDTO(
        Long tipoAtributoId,
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
