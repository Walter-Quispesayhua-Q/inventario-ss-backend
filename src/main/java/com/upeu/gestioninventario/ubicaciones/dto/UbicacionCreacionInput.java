package com.upeu.gestioninventario.ubicaciones.dto;


public record UbicacionCreacionInput(
        String nombreSimple,
        String edificio,
        String piso,
        String oficinaAmbiente,
        String descripcion
) {
}