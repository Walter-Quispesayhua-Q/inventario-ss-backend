package com.upeu.gestioninventario.importacion.dto.formato.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ColumnasEspecialesDTO(
        IgnorarColumnasDTO ignorar
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IgnorarColumnasDTO(
            Boolean habilitado,
            List<String> columnas,
            Boolean deteccionAutomatica,
            List<String> patronesIgnorar
    ) {}
}

