package com.upeu.gestioninventario.estructuras.dto.estacion;

public record BienAsignadoDTO(
    Long idBien,
    String nombre,
    String caf,
    String numeroSerie,
    Long categoriaId,
    String categoriaNombre,
    String estadoFisico,
    String estadoOperacional,
    String marca,
    String modelo,
    String responsableActualNombre,
    Long idEstacionAnterior,
    String nombreEstacionAnterior
) {}
