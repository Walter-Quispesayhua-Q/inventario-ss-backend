package com.upeu.gestioninventario.ml.dto.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AtributoExtraccionSeedDTO(
        @JsonProperty("nombreCampo") String nombreCampo,
        String etiqueta,
        @JsonProperty("tipoDato") String tipoDato,
        Boolean requerido,
        Integer prioridad,
        @JsonProperty("orden_ui") Integer ordenUI,
        String placeholder,
        @JsonProperty("validacion") Map<String, Object> validacion,
        @JsonProperty("opciones") Object opciones,
        @JsonProperty("extraccion") Map<String, Object> extraccion,
        @JsonProperty("valorPorDefecto") String valorPorDefecto,
        Integer maxLength
) {}