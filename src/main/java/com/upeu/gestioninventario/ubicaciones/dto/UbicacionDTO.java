package com.upeu.gestioninventario.ubicaciones.dto;


public record UbicacionDTO(
        Long id,
        String nombreUbicacion,
        String descripcion,
        String edificio,
        String piso,
        String oficinaAmbiente
) {
}