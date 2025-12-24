package com.upeu.gestioninventario.estructuras.dto;

import com.upeu.gestioninventario.estructuras.dto.estacion.BienAsignadoDTO;

import java.util.List;

public record BienesParaEstacionResponseDTO(
        List<BienAsignadoDTO> bienesLibres,
        List<BienAsignadoDTO> bienesEnOtrasEstaciones,
        List<BienAsignadoDTO> bienesEnEstacionActual,
        int totalLibres,
        int totalEnOtras,
        int totalEnActual
) {
    public static BienesParaEstacionResponseDTO of(
            List<BienAsignadoDTO> libres,
            List<BienAsignadoDTO> enOtras,
            List<BienAsignadoDTO> enActual) {
        return new BienesParaEstacionResponseDTO(
                libres,
                enOtras,
                enActual,
                libres.size(),
                enOtras.size(),
                enActual.size()
        );
    }
}
