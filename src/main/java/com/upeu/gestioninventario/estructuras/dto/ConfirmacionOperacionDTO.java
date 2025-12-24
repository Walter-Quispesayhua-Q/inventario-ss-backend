package com.upeu.gestioninventario.estructuras.dto;

public record ConfirmacionOperacionDTO(
    String codigoConfirmacion,
    String mensaje,
    boolean confirmado
) {
}
