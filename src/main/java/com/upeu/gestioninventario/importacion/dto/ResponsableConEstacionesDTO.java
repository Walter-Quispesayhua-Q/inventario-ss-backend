package com.upeu.gestioninventario.importacion.dto;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;

import java.util.List;

public record ResponsableConEstacionesDTO(
        String nombreResponsable,
        boolean esUsuarioLogueado,
        int totalEstaciones,
        int totalBienes,
        List<EstacionConComponentesDTO> estaciones
) {
    public static ResponsableConEstacionesDTO crear(
            String nombreResponsable,
            boolean esUsuarioLogueado,
            List<EstacionConComponentesDTO> estaciones) {
        
        int totalBienes = estaciones.stream()
                .mapToInt(e -> e.getTotalBienes() != null ? e.getTotalBienes() : 0)
                .sum();
        
        return new ResponsableConEstacionesDTO(
                nombreResponsable,
                esUsuarioLogueado,
                estaciones.size(),
                totalBienes,
                estaciones
        );
    }
}
