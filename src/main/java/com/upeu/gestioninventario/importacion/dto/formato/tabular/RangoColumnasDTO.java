package com.upeu.gestioninventario.importacion.dto.formato.tabular;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record RangoColumnasDTO(
        String inicio,
        String fin,
        Boolean deteccionAutomatica
) {
    public RangoColumnasDTO {
        if (inicio == null) inicio = "A";
        if (deteccionAutomatica == null) deteccionAutomatica = true;
    }
}
