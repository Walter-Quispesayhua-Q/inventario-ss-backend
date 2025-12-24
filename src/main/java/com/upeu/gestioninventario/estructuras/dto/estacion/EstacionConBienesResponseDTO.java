package com.upeu.gestioninventario.estructuras.dto.estacion;

import com.upeu.gestioninventario.estructuras.dto.ComponenteResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;

import java.util.List;
import java.util.Map;

public record EstacionConBienesResponseDTO(
    Long idEstacion,
    String nombre,
    String codigo,
    Long idAmbiente,
    String nombreAmbiente,
    Long idPiso,
    String nombrePiso,
    Integer numeroPiso,
    Long idEdificio,
    String nombreEdificio,
    TipoEstructuraDTO tipoEstructura,
    Integer capacidadBienes,
    Integer capacidadDisponible,
    Integer cantidadBienesAsignados,
    String responsableNombre,
    List<ComponenteResumenDTO> componentes,
    List<BienAsignadoDTO> bienesIndividuales,
    Integer totalComponentes,
    Integer totalBienes,
    Integer totalBienesIndividuales,
    Map<String, Object> propiedadesAdicionales,
    String observaciones
) {}
