package com.upeu.gestioninventario.categorias.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PlantillaAtributoCreacionDTO(
    @NotNull(message = "El ID de plantilla es requerido")
    Long idPlantilla,
    
    @NotNull(message = "El ID del tipo de atributo es requerido")
    Long idTipoAtributo,
    
    @NotBlank(message = "La etiqueta es requerida")
    String etiqueta,
    
    @JsonProperty("esRequerido")
    Boolean esRequerido,
    
    @JsonProperty("ordenUI")
    Integer ordenUI,
    
    @JsonProperty("jsonExtraccion")
    Map<String, Object> jsonExtraccion
) {
    public static PlantillaAtributoCreacionDTO basico(
        Long idPlantilla,
        Long idTipoAtributo,
        String etiqueta
    ) {
        return new PlantillaAtributoCreacionDTO(
            idPlantilla, idTipoAtributo, etiqueta,
            false, null, null
        );
    }
}
