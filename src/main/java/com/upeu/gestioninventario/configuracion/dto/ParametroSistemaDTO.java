package com.upeu.gestioninventario.configuracion.dto;

import java.time.LocalDateTime;

public record ParametroSistemaDTO(
        Long id,
        String clave,
        String valor,
        String descripcion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaUltimaModificacion
) {
}