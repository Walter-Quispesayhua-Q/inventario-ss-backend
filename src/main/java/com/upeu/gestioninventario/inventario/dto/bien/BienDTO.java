package com.upeu.gestioninventario.inventario.dto.bien;

import com.upeu.gestioninventario.ubicaciones.dto.UbicacionDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record BienDTO(
        Long id,
        String nombreBien,
        String caf,
        String numeroSerie,
        String observaciones,

        @JsonProperty("estadoFisico")
        String estadoFisico,

        @JsonProperty("estadoOperacional")
        String estadoOperacional,

        LocalDateTime fechaCreacion,
        String usuarioCreacionNombre,
        LocalDateTime fechaUltimaModificacion,
        String usuarioUltimaModificacionNombre,

        Long categoriaId,
        String categoriaNombre,
        String categoriaIcono,
        String categoriaColor,

        UbicacionDTO ubicacionActual,

        Boolean estaEnEstacion,
        Long estacionActualId,
        String estacionActualNombre,
        String estacionActualCodigo,

        Long responsableActualId,
        String responsableActualNombre,

        Long departamentoId,
        String departamentoNombre,

        @JsonProperty("atributosValores")
        List<BienAtributoValorDTO> atributosValores
) {
}