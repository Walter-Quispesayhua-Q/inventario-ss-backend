package com.upeu.gestioninventario.ml.dto.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MetadatosSeedDTO(
        @JsonProperty("idPlantilla") String idPlantilla,
        String version,
        String nombre,
        @JsonProperty("categoriaSemantica") String categoriaSemantica,
        String descripcion
) {}