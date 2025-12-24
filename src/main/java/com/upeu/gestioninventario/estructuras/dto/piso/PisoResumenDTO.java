package com.upeu.gestioninventario.estructuras.dto.piso;

import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;

public record PisoResumenDTO(
    Long idPiso,
    String nombre,
    String codigo,
    Integer numeroPiso,
    TipoEstructuraDTO tipoEstructura,
    Long cantidadAmbientes
) {}
