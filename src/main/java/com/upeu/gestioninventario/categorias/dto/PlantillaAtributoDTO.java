package com.upeu.gestioninventario.categorias.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record PlantillaAtributoDTO(
        Long id,
        Long idPlantilla,
        Long idTipoAtributo,
        String etiqueta,

        @JsonProperty("esRequerido")
        Boolean esRequerido,

        @JsonProperty("ordenUI")
        Integer ordenUI,

        @JsonProperty("jsonExtraccion")
        Map<String, Object> jsonExtraccion,

        String nombreAtributo,
        String tipoDato
) {
    public static PlantillaAtributoDTO simple(
            Long id,
            Long idPlantilla,
            Long idTipoAtributo,
            String etiqueta,
            Boolean esRequerido
    ) {
        return new PlantillaAtributoDTO(
                id, idPlantilla, idTipoAtributo, etiqueta,
                esRequerido, null, null, null, null
        );
    }
}