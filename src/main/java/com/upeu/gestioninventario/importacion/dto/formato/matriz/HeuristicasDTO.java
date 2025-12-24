package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeuristicasDTO(
        Boolean habilitadas,
        DeteccionFormatoDTO deteccionFormato,
        PatronesDTO patrones,
        FallbackDTO fallback
) {
    public HeuristicasDTO {
        if (habilitadas == null) habilitadas = true;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeteccionFormatoDTO(
            Integer analizarFilasMinimo,
            Integer analizarFilasMaximo,
            Boolean analizarHojaCompleta,
            Double umbralConfianza
    ) {
        public DeteccionFormatoDTO {
            if (analizarFilasMinimo == null) analizarFilasMinimo = 100;
            if (analizarHojaCompleta == null) analizarHojaCompleta = true;
            if (umbralConfianza == null) umbralConfianza = 0.6;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PatronesDTO(
            TitulosFichaDTO titulosFicha,
            FinDeSeccionDTO finDeSeccion,
            MetadatosVsTablaDTO metadatosVsTabla
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TitulosFichaDTO(
            Boolean usarRegex,
            Boolean usarCeldasCombinadas,
            Boolean usarEstilos,
            String priorizarPor
    ) {
        public TitulosFichaDTO {
            if (usarRegex == null) usarRegex = true;
            if (usarCeldasCombinadas == null) usarCeldasCombinadas = true;
            if (usarEstilos == null) usarEstilos = true;
            if (priorizarPor == null) priorizarPor = "REGEX_PRIMERO";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinDeSeccionDTO(
            Integer filasVacias,
            Boolean cambioDePatron,
            Boolean nuevaCeldaCombinada
    ) {
        public FinDeSeccionDTO {
            if (filasVacias == null) filasVacias = 1;
            if (cambioDePatron == null) cambioDePatron = true;
            if (nuevaCeldaCombinada == null) nuevaCeldaCombinada = true;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetadatosVsTablaDTO(
            Boolean metadatosTienen2Columnas,
            Boolean tablaTieneMasColumnas
    ) {
        public MetadatosVsTablaDTO {
            if (metadatosTienen2Columnas == null) metadatosTienen2Columnas = true;
            if (tablaTieneMasColumnas == null) tablaTieneMasColumnas = true;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FallbackDTO(
            String siNoDetectaFichas,
            String siErrorEnFicha,
            String siMetadatosVacios
    ) {
        public FallbackDTO {
            if (siNoDetectaFichas == null) siNoDetectaFichas = "PROCESAR_COMO_TABULAR";
            if (siErrorEnFicha == null) siErrorEnFicha = "CONTINUAR_CON_SIGUIENTE";
            if (siMetadatosVacios == null) siMetadatosVacios = "USAR_VALORES_DEFAULT";
        }
    }
}