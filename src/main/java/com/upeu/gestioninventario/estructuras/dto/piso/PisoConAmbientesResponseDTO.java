package com.upeu.gestioninventario.estructuras.dto.piso;

import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;

import java.util.List;

public record PisoConAmbientesResponseDTO(
    Long idPiso,
    String nombre,
    String codigo,
    Integer numeroPiso,
    TipoEstructuraDTO tipoEstructura,
    Long idEdificio,
    String nombreEdificio,
    String codigoEdificio,
    Long idResponsableMantenimiento,
    String nombreResponsableMantenimiento,
    String observaciones,
    List<AmbienteResumenDTO> ambientes,
    Long totalAmbientes
) {}
