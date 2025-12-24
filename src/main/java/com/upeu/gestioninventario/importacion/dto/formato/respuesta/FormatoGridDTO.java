package com.upeu.gestioninventario.importacion.dto.formato.respuesta;

public record FormatoGridDTO(
        String rangoInicio,
        String rangoFin,
        int filaHeaders,
        int columnaInicio,
        int columnaClave
) {
    public static FormatoGridDTO crear(String inicio, String fin, int filaHeaders, int colInicio) {
        return new FormatoGridDTO(inicio, fin, filaHeaders, colInicio, 0);
    }

    public static FormatoGridDTO paraMatriz(String inicio, String fin, int filaHeaders, int colClave) {
        return new FormatoGridDTO(inicio, fin, filaHeaders, 0, colClave);
    }
}