package com.upeu.gestioninventario.importacion.dto.formato.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MetadataReconocimientoDTO(
        String titulo,
        String regex,
        Boolean esFicha,
        Integer puntuacion
) {
    public MetadataReconocimientoDTO {
        if (esFicha == null) esFicha = false;
        if (puntuacion == null) puntuacion = 0;
    }

}
