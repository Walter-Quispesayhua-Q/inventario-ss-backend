package com.upeu.gestioninventario.importacion.dto.formato.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CeldasCombinadasDTO(
        Boolean soportar,
        Boolean detectarAutomatico,
        String propagarValor,
        Boolean usarComoAgrupador,
        Boolean usarComoTituloFicha
) {

    public CeldasCombinadasDTO {
        if (soportar == null) soportar = true;
        if (detectarAutomatico == null) detectarAutomatico = true;
        if (propagarValor == null) propagarValor = "HACIA_ABAJO";
        if (usarComoAgrupador == null) usarComoAgrupador = true;
        if (usarComoTituloFicha == null) usarComoTituloFicha = true;
    }
}
