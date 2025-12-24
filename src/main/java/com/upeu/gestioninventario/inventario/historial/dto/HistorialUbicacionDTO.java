package com.upeu.gestioninventario.inventario.historial.dto;

import java.time.LocalDateTime;

public record HistorialUbicacionDTO(
        Long id,
        LocalDateTime fechaEntrada,
        LocalDateTime fechaSalida,
        LocalDateTime fechaRegistro,
        Long bienId,
        String bienNombre,
        String bienCaf,

        Long ubicacionId,
        String ubicacionNombre,

        Long usuarioRegistroId,
        String usuarioRegistroNombre
) {
}