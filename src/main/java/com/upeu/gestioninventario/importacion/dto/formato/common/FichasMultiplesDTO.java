package com.upeu.gestioninventario.importacion.dto.formato.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FichasMultiplesDTO(
        Boolean habilitadas,
        Boolean detectarAutomatico,
        String prioridad,
        SeparadoresDTO separadores,
        Boolean analizarHojaCompleta,
        ValidacionDTO validacion
) {
    public FichasMultiplesDTO {
        if (habilitadas == null) habilitadas = true;
        if (detectarAutomatico == null) detectarAutomatico = true;
        if (prioridad == null) prioridad = "PATRON_PRIMERO";
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SeparadoresDTO(
            PorPatronDTO porPatron,
            PorFilasVaciasDTO porFilasVacias,
            PorCeldaCombinadaDTO porCeldaCombinada,
            PorEstiloCeldaDTO porEstiloCelda
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PorPatronDTO(
            Boolean habilitado,
            List<String> patrones,
            List<String> textoExacto,
            String detectarEnColumna
    ) {
        public PorPatronDTO {
            if (habilitado == null) habilitado = true;
            if (patrones == null) patrones = List.of(
                "^[A-Z]{2,10}-\\d{1,5}$",    // PC-01, LAPTOP-123
                "^[A-Z]{2,10}_\\d{1,5}$",    // PC_01, LAPTOP_123
                "^\\d{4,12}$",               // Códigos numéricos: 123456789
                "^INV-\\d{4}-\\d{3,5}$",     // INV-2024-001
                "^[A-Z]+-[A-Z]+-\\d+$"       // LAB-PC-001
            );
            if (textoExacto == null) textoExacto = List.of("===", "---", "***");
            if (detectarEnColumna == null) detectarEnColumna = "A";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PorFilasVaciasDTO(
            Boolean habilitado,
            Integer minimoFilasVacias,
            Integer maximoFilasVaciasConsecutivas,
            Integer maximoFilasVacias
    ) {
        public PorFilasVaciasDTO {
            if (habilitado == null) habilitado = true;
            if (minimoFilasVacias == null) minimoFilasVacias = 1;
            if (maximoFilasVaciasConsecutivas == null) maximoFilasVaciasConsecutivas = 3;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PorCeldaCombinadaDTO(
            Boolean habilitado,
            Integer minimoColumnasAbarcadas,
            Boolean considerarComoNuevaFicha
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PorEstiloCeldaDTO(
            Boolean habilitado,
            Boolean detectarNegrita,
            Boolean detectarFondoColoreado
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ValidacionDTO(
            Integer minimoFilasPorFicha,
            Integer maximoFilasPorFicha,
            Boolean ignorarFichasVacias,
            Integer minimoComponentesPorFicha
    ) {}
}
