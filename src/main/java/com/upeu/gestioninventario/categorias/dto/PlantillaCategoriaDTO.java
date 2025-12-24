package com.upeu.gestioninventario.categorias.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record PlantillaCategoriaDTO(
        Long id,

        @JsonProperty("idPlantillaSeed")
        String idPlantillaSeed,

        @JsonProperty("nombrePlantilla")
        String nombrePlantilla,

        String version,
        String descripcion,

        @JsonProperty("categoriaSemantica")
        String categoriaSemantica,

        // Campos JSONB
        @JsonProperty("jsonScoring")
        Map<String, Object> jsonScoring,

        @JsonProperty("jsonReconocimiento")
        Map<String, Object> jsonReconocimiento,

        @JsonProperty("jsonPatrones")
        Map<String, Object> jsonPatrones,

        @JsonProperty("jsonInteligencia")
        Map<String, Object> jsonInteligencia,

        @JsonProperty("jsonAprendizaje")
        Map<String, Object> jsonAprendizaje,

        Set<PlantillaAtributoDTO> plantillaAtributos,

        Long idUsuarioCreacion,
        Long idUsuarioUltimaModificacion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaUltimaModificacion
) {
    public static PlantillaCategoriaDTO basico(Long id, String nombrePlantilla, String version) {
        return new PlantillaCategoriaDTO(
                id, null, nombrePlantilla, version, null, null,
                null, null, null, null, null,
                null, null, null, null, null
        );
    }
}