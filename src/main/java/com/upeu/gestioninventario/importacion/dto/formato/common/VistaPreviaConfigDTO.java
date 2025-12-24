package com.upeu.gestioninventario.importacion.dto.formato.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VistaPreviaConfigDTO(
        Boolean mostrarCoordenadasOriginal,
        Boolean mostrarHojasDisponibles,
        Boolean mostrarTipoDetectado,
        Boolean resaltarCeldasMapeadas,
        Integer maxFilasPreview,
        Boolean resaltarClavesDetectadas,
        Boolean mostrarSeccionesColapsables,
        Integer maxParesPreview,
        Boolean mostrarComoTabla,
        Boolean resaltarEncabezados,
        Boolean mostrarAgrupaciones,
        Integer maxColumnasPreview,
        Boolean resaltarTitulosFicha,
        Boolean resaltarMetadatos,
        Boolean mostrarSeparadoresFicha,
        Boolean mostrarResumenFichas
) {
    public VistaPreviaConfigDTO {
        if (mostrarCoordenadasOriginal == null) mostrarCoordenadasOriginal = true;
        if (mostrarHojasDisponibles == null) mostrarHojasDisponibles = true;
        if (mostrarTipoDetectado == null) mostrarTipoDetectado = true;
        if (resaltarCeldasMapeadas == null) resaltarCeldasMapeadas = true;
        if (maxFilasPreview == null) maxFilasPreview = 50;
        if (resaltarClavesDetectadas == null) resaltarClavesDetectadas = true;
        if (mostrarSeccionesColapsables == null) mostrarSeccionesColapsables = true;
        if (maxParesPreview == null) maxParesPreview = 100;
        if (mostrarComoTabla == null) mostrarComoTabla = true;
        if (resaltarEncabezados == null) resaltarEncabezados = true;
        if (mostrarAgrupaciones == null) mostrarAgrupaciones = true;
        if (maxColumnasPreview == null) maxColumnasPreview = 20;
        if (resaltarTitulosFicha == null) resaltarTitulosFicha = true;
        if (resaltarMetadatos == null) resaltarMetadatos = true;
        if (mostrarSeparadoresFicha == null) mostrarSeparadoresFicha = true;
        if (mostrarResumenFichas == null) mostrarResumenFichas = true;
    }
}