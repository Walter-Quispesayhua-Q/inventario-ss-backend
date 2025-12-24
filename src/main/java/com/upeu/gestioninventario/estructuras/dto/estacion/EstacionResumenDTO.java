package com.upeu.gestioninventario.estructuras.dto.estacion;

import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;

public record EstacionResumenDTO(
    Long idEstacion,
    String nombre,
    String codigo,
    Integer capacidadBienes,
    Long cantidadBienesAsignados,
    Integer capacidadDisponible,
    TipoEstructuraDTO tipoEstructura,
    String observaciones
) {}
