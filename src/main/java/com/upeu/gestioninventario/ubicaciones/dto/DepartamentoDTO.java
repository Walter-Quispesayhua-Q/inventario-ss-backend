package com.upeu.gestioninventario.ubicaciones.dto;

import java.time.LocalDateTime;

public record DepartamentoDTO(
        Long id,
        String nombreDepartamento,
        String descripcion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaUltimaModificacion
) {
}