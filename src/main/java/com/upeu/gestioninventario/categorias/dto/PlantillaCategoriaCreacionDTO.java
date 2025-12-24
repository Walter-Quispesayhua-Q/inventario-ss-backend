package com.upeu.gestioninventario.categorias.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PlantillaCategoriaCreacionDTO(
        @NotBlank(message = "El ID de plantilla seed es requerido")
        @JsonProperty("idPlantillaSeed")
        String idPlantillaSeed,

        @NotBlank(message = "El nombre de plantilla es requerido")
        @JsonProperty("nombrePlantilla")
        String nombrePlantilla,

        @NotBlank(message = "La versión es requerida")
        String version,

        String descripcion,

        @NotBlank(message = "La categoría semántica es requerida")
        @JsonProperty("categoriaSemantica")
        String categoriaSemantica,

        @NotNull(message = "La configuración de scoring es requerida")
        @JsonProperty("jsonScoring")
        Map<String, Object> jsonScoring,

        @NotNull(message = "La configuración de reconocimiento es requerida")
        @JsonProperty("jsonReconocimiento")
        Map<String, Object> jsonReconocimiento,

        @JsonProperty("jsonPatrones")
        Map<String, Object> jsonPatrones,

        @NotNull(message = "La configuración de inteligencia es requerida")
        @JsonProperty("jsonInteligencia")
        Map<String, Object> jsonInteligencia,

        @JsonProperty("jsonAprendizaje")
        Map<String, Object> jsonAprendizaje
) {}