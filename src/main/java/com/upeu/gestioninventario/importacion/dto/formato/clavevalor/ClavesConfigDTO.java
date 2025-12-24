package com.upeu.gestioninventario.importacion.dto.formato.clavevalor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClavesConfigDTO(
        String columna,
        Integer filaInicio,
        Integer filaFin,
        Boolean deteccionAutomatica
) {
    public ClavesConfigDTO {
        if (columna == null) columna = "A";
        if (filaInicio == null) filaInicio = 1;
        if (deteccionAutomatica == null) deteccionAutomatica = true;
    }
}
