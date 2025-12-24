package com.upeu.gestioninventario.estructuras.dto.edificio;

import com.upeu.gestioninventario.estructuras.dto.piso.PisoResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;

import java.util.List;

public record EdificioConPisosResponseDTO(
    Long idEdificio,
    String nombre,
    String codigo,
    TipoEstructuraDTO tipoEstructura,
    String direccion,
    Integer numeroPisos,
    Long idResponsableMantenimiento,
    String nombreResponsableMantenimiento,
    String observaciones,
    List<PisoResumenDTO> pisos,
    Long totalPisos
) {}
