package com.upeu.gestioninventario.ml.dto.plantilla;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CampoMetadata(
        @JsonProperty("titulo")
        String titulo,

        @JsonProperty("regex")
        String regex,

        @JsonProperty("esFicha")
        Boolean esFicha,

        @JsonProperty("puntuacion")
        Integer puntuacion
) {
}
