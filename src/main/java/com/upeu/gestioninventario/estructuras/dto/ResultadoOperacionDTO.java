package com.upeu.gestioninventario.estructuras.dto;

public record ResultadoOperacionDTO(
    boolean exito,
    String mensaje,
    String codigoOperacion,
    Object datos
) {
    public static ResultadoOperacionDTO exitoso(String mensaje) {
        return new ResultadoOperacionDTO(true, mensaje, null, null);
    }

    public static ResultadoOperacionDTO requiereConfirmacion(String codigo, String mensaje) {
        return new ResultadoOperacionDTO(false, mensaje, codigo, null);
    }
}
