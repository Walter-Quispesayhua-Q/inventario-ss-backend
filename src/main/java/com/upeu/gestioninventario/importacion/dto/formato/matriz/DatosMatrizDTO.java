package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosMatrizDTO(
        String celdaInicio,
        String celdaFin,
        Boolean deteccionAutomatica,
        String tipoDatoEsperado
) {
    public DatosMatrizDTO {
        if (celdaInicio == null) celdaInicio = "B2";
        if (deteccionAutomatica == null) deteccionAutomatica = true;
        if (tipoDatoEsperado == null) tipoDatoEsperado = "numero";
    }
}
