package com.upeu.gestioninventario.importacion.dto.formato.respuesta;

import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;

public record MetadatosHojaDTO(
        TipoFormatoDetectado tipoFormato,
        String nombreHoja,
        int indiceHoja,
        int totalFilas,
        int totalColumnas,
        int scoreDeteccion
) {
    public static MetadatosHojaDTO crear(
            TipoFormatoDetectado tipo,
            String nombre,
            int indice,
            int filas,
            int columnas,
            int score) {
        return new MetadatosHojaDTO(tipo, nombre, indice, filas, columnas, score);
    }
}