package com.upeu.gestioninventario.importacion.dto.formato.respuesta;

import java.util.List;
import java.util.Map;

public record EstructuraHojaDTO(
        List<String> headersColumnas,
        List<String> headersFilas,
        List<Map<String, String>> metadatosPorFila
) {
    public static EstructuraHojaDTO soloColumnas(List<String> headers) {
        return new EstructuraHojaDTO(headers, List.of(), List.of());
    }

    public static EstructuraHojaDTO paraTabular(List<String> headers) {
        return new EstructuraHojaDTO(headers, List.of(), List.of());
    }


    public static EstructuraHojaDTO paraMatrizConMetadatos(
            List<String> headersColumnas,
            List<String> headersFilas,
            List<Map<String, String>> metadatos) {
        return new EstructuraHojaDTO(headersColumnas, headersFilas, metadatos);
    }
}