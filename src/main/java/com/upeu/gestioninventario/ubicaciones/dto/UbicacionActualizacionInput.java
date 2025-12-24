package com.upeu.gestioninventario.ubicaciones.dto;


public record UbicacionActualizacionInput(
        String descripcion,
        String edificio,
        String piso,
        String oficinaAmbiente
) {
}