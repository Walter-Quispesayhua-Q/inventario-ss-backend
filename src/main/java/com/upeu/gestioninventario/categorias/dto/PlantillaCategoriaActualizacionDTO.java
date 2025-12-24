package com.upeu.gestioninventario.categorias.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record PlantillaCategoriaActualizacionDTO(
    @NotBlank(message = "El nombre de plantilla es requerido")
    @JsonProperty("nombrePlantilla")
    String nombrePlantilla,
    
    String version,
    String descripcion,
    
    @JsonProperty("categoriaSemantica")
    String categoriaSemantica,
    
    @JsonProperty("jsonScoring")
    Map<String, Object> jsonScoring,
    
    @JsonProperty("jsonReconocimiento")
    Map<String, Object> jsonReconocimiento,
    
    @JsonProperty("jsonPatrones")
    Map<String, Object> jsonPatrones,
    
    @JsonProperty("jsonInteligencia")
    Map<String, Object> jsonInteligencia,
    
    @JsonProperty("jsonAprendizaje")
    Map<String, Object> jsonAprendizaje
) {
    public static PlantillaCategoriaActualizacionDTO soloNombre(String nombrePlantilla) {
        return new PlantillaCategoriaActualizacionDTO(
            nombrePlantilla, null, null, null, 
            null, null, null, null, null
        );
    }

    public static PlantillaCategoriaActualizacionDTO soloML(
        Map<String, Object> jsonScoring,
        Map<String, Object> jsonInteligencia,
        Map<String, Object> jsonAprendizaje
    ) {
        return new PlantillaCategoriaActualizacionDTO(
            null, null, null, null,
            jsonScoring, null, null, jsonInteligencia, jsonAprendizaje
        );
    }
}
