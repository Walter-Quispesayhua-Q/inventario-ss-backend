package com.upeu.gestioninventario.ml.dto.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlantillaSeedDTO(
        @JsonProperty("metadatos") MetadatosSeedDTO metadatos,
        @JsonProperty("inteligencia") Map<String, Object> inteligencia,
        @JsonProperty("scoring") Map<String, Object> scoring,
        @JsonProperty("reconocimiento") Map<String, Object> reconocimiento,
        @JsonProperty("patronesRegex") Map<String, Object> patronesRegex,
        @JsonProperty("atributosExtraccion") List<AtributoExtraccionSeedDTO> atributosExtraccion,
        @JsonProperty("aprendizaje") Map<String, Object> aprendizaje,
        @JsonProperty("casosDePruebaBase") List<Map<String, Object>> casosDePruebaBase
) {}