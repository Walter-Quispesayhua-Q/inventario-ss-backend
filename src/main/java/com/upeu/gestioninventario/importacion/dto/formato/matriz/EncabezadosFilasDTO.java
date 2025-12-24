package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EncabezadosFilasDTO(
        String columna,
        Integer filaInicio,
        Integer filaFin,
        Boolean deteccionAutomatica
) {
    public EncabezadosFilasDTO {
        if (columna == null) columna = "A";
        if (filaInicio == null) filaInicio = 2;
        if (deteccionAutomatica == null) deteccionAutomatica = true;
    }
}
