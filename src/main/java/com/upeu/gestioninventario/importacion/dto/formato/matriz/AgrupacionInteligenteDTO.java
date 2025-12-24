package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgrupacionInteligenteDTO(
        Boolean habilitada,
        String columnaIndice,
        Boolean usarPlantillasParaDeteccion,
        HerenciaConfigDTO herencia
) {
    public AgrupacionInteligenteDTO {
        if (habilitada == null) habilitada = true;
        if (columnaIndice == null) columnaIndice = "A";
        if (usarPlantillasParaDeteccion == null) usarPlantillasParaDeteccion = true;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HerenciaConfigDTO(
            String itemsSinIndice,
            Boolean indiceNuevoIniciaNuevoGrupo
    ) {
        public HerenciaConfigDTO {
            if (itemsSinIndice == null) itemsSinIndice = "ASOCIAR_A_ULTIMO_INDICE";
            if (indiceNuevoIniciaNuevoGrupo == null) indiceNuevoIniciaNuevoGrupo = true;
        }
    }
}
