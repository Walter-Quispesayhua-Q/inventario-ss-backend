package com.upeu.gestioninventario.importacion.dto;

public record FilaErrorDTO(
        int numeroFila,
        String codigoCaf,
        String nombreBien,
        String razonError
) {
}