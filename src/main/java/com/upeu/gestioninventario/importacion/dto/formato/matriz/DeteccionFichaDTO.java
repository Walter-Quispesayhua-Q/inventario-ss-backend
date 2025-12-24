package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeteccionFichaDTO(
        Boolean habilitada,
        Boolean analizarHojaCompleta,
        List<String> estrategias,
        String prioridadEstrategia,
        PorPatronDTO porPatron,
        PorCeldaCombinadaDTO porCeldaCombinada,
        PorEstiloDTO porEstilo
) {
    public DeteccionFichaDTO {
        if (habilitada == null) habilitada = true;
        if (analizarHojaCompleta == null) analizarHojaCompleta = true;
        if (estrategias == null) estrategias = List.of("PATRON", "CELDA_COMBINADA", "ESTILO");
        if (prioridadEstrategia == null) prioridadEstrategia = "PATRON_PRIMERO";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PorPatronDTO(
            Boolean habilitado,
            String columna,
            List<String> patrones,
            List<String> textoExacto
    ) {
        public PorPatronDTO {
            if (habilitado == null) habilitado = true;
            if (columna == null) columna = "A";
            if (patrones == null) patrones = List.of("^PC-\\d{1,3}$", "^[A-Z]{2,5}-\\d{1,5}$");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PorCeldaCombinadaDTO(
            Boolean habilitado,
            Integer minimoColumnasAbarcadas,
            Boolean considerarComoTitulo,
            Boolean ignorarSiVacia
    ) {
        public PorCeldaCombinadaDTO {
            if (habilitado == null) habilitado = true;
            if (minimoColumnasAbarcadas == null) minimoColumnasAbarcadas = 2;
            if (considerarComoTitulo == null) considerarComoTitulo = true;
            if (ignorarSiVacia == null) ignorarSiVacia = true;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PorEstiloDTO(
            Boolean habilitado,
            Boolean detectarNegrita,
            Boolean detectarFondoColor,
            Boolean detectarFuenteGrande,
            Integer tamanoFuenteMinimo
    ) {
        public PorEstiloDTO {
            if (habilitado == null) habilitado = true;
            if (detectarNegrita == null) detectarNegrita = true;
            if (detectarFondoColor == null) detectarFondoColor = true;
            if (detectarFuenteGrande == null) detectarFuenteGrande = true;
            if (tamanoFuenteMinimo == null) tamanoFuenteMinimo = 12;
        }
    }
}