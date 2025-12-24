package com.upeu.gestioninventario.importacion.dto.formato.tabular;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosConfigDTO(
        Integer filaInicio,
        Integer filaFin
) {
}
