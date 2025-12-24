package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MapeoAtributosDTO(
        Boolean habilitado,
        Boolean ignorarMayusculas,
        Boolean usarAlias,
        Map<String, List<String>> atributosBase
) {
    public MapeoAtributosDTO {
        if (habilitado == null) habilitado = true;
        if (ignorarMayusculas == null) ignorarMayusculas = true;
        if (usarAlias == null) usarAlias = true;
    }
}