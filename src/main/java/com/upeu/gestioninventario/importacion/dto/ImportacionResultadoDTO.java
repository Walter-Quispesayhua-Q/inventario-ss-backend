package com.upeu.gestioninventario.importacion.dto;

import com.upeu.gestioninventario.estructuras.dto.ResultadoCreacionEstacionDTO;

import java.util.List;

public record ImportacionResultadoDTO(
        int bienesCreados,
        int bienesActualizados,
        int totalFallidos,
        int filasConError,
        String mensaje,
        List<FilaErrorDTO> errores,
        List<ImportacionItemResultadoDTO> detalles,
        List<ImportacionItemResultadoDTO> itemsFallidos,
        int estacionesCreadas,
        int estacionesActualizadas,
        int estacionesFallidas,
        int bienesAsignadosAEstaciones,
        List<ResultadoCreacionEstacionDTO> detallesEstaciones
) {
}