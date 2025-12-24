package com.upeu.gestioninventario.estructuras.dto.ambiente;

import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;

public record AmbienteResumenDTO(
    Long idAmbiente,
    String nombre,
    String codigo,
    Integer capacidadPersonas,
    TipoEstructuraDTO tipoEstructura,
    String nombreResponsable,
    Long cantidadEstaciones
) {}
