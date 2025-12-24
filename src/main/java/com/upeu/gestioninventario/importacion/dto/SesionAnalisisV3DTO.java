package com.upeu.gestioninventario.importacion.dto;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.HojaAnalizadaDTO;

import java.util.List;
import java.util.Map;

public record SesionAnalisisV3DTO(
        String sessionId,
        String nombreArchivo,
        List<HojaAnalizadaDTO> hojas,
        Map<String, Integer> categoriasDetectadas
) {

    public List<FilaPreviewDTO> todasLasFilas() {
        if (hojas == null) return List.of();
        return hojas.stream()
                .filter(h -> h.filasEnriquecidas() != null)
                .flatMap(h -> h.filasEnriquecidas().stream())
                .toList();
    }

    public List<EstacionConComponentesDTO> todasLasEstaciones() {
        if (hojas == null) return List.of();
        return hojas.stream()
                .filter(h -> h.estacionesSugeridas() != null)
                .flatMap(h -> h.estacionesSugeridas().stream())
                .toList();
    }

}
