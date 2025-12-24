package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EncabezadosColumnasDTO(
        Integer fila,
        String columnaInicio,
        String columnaFin,
        Boolean deteccionAutomatica
) {
    public EncabezadosColumnasDTO {
        if (fila == null) fila = 1;
        if (columnaInicio == null) columnaInicio = "B";
        if (deteccionAutomatica == null) deteccionAutomatica = true;
    }
}
