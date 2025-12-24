package com.upeu.gestioninventario.estructuras.dto;

import com.upeu.gestioninventario.estructuras.dto.estacion.BienAsignadoDTO;

import java.util.List;

public record ComponenteResumenDTO(
    Long id,
    String nombre,
    String tipo,
    String categoriaBase,
    Integer orden,
    List<BienAsignadoDTO> bienes,
    Integer totalBienes
) {}
