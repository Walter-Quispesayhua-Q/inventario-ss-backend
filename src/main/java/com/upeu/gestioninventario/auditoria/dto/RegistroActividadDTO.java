package com.upeu.gestioninventario.auditoria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Map;

public record RegistroActividadDTO(
        Long id,
        String tipoOperacion,
        String tipoEndpoint,
        String entidadAfectada,
        Long entidadId,
        String entidadNombre,

        String descripcion,

        @JsonProperty("metadatos")
        Map<String, Object> metadatos,

        OffsetDateTime fechaOperacion,
        Long usuarioId,
        String usuarioNombre,
        String usuarioEmail
) {
}
