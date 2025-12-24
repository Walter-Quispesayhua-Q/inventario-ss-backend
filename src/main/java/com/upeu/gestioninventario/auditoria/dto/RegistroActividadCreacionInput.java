package com.upeu.gestioninventario.auditoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record RegistroActividadCreacionInput(
        @NotNull(message = "El tipo de operación no puede ser nulo")
        String tipoOperacion,

        @NotNull(message = "El tipo de endpoint no puede ser nulo")
        String tipoEndpoint,

        String entidadAfectada,

        Long entidadId,

        @NotBlank(message = "La descripción no puede estar vacía")
        String descripcion,

        Map<String, Object> metadatos,

        String ipAddress,

        String userAgent
) {
}
