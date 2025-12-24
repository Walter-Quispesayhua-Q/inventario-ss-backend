package com.upeu.gestioninventario.importacion.dto.formato.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PreservarCoordenadasDTO(
        Boolean habilitado,
        Boolean incluirCeldasVacias,
        String formatoCoordenada,
        Boolean incluirInfoFicha
) {
    public PreservarCoordenadasDTO {
        if (habilitado == null) habilitado = true;
        if (incluirCeldasVacias == null) incluirCeldasVacias = false;
        if (formatoCoordenada == null) formatoCoordenada = "A1";
        if (incluirInfoFicha == null) incluirInfoFicha = true;
    }
}
