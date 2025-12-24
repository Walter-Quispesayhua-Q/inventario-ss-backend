package com.upeu.gestioninventario.importacion.dto.formato.clavevalor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ValoresConfigDTO(
        String columna,
        Integer filaInicio,
        Integer filaFin,
        Boolean permitirMultiplesColumnas
) {
    public ValoresConfigDTO {
        if (columna == null) columna = "B";
        if (filaInicio == null) filaInicio = 1;
        if (permitirMultiplesColumnas == null) permitirMultiplesColumnas = false;
    }
}
