package com.upeu.gestioninventario.importacion.dto.formato.tabular;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EncabezadosConfigDTO(
        Integer fila,
        Boolean deteccionAutomatica,
        Integer filasAEscanear,
        Double umbralDensidad
) {
    public EncabezadosConfigDTO {
        if (deteccionAutomatica == null) deteccionAutomatica = true;
        if (filasAEscanear == null) filasAEscanear = 25;
        if (umbralDensidad == null) umbralDensidad = 0.5;
    }
}
