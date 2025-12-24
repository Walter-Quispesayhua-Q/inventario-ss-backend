package com.upeu.gestioninventario.importacion.dto.formato.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HojasConfigDTO(
        List<Integer> indices,
        Boolean detectarTodas,
        Map<String, String> nombresPorIndice,
        List<String> nombresFichasEsperados
) {

    public HojasConfigDTO {
        if (indices == null) indices = List.of(0);
        if (detectarTodas == null) detectarTodas = false;
        if (nombresPorIndice == null) nombresPorIndice = Map.of();
        if (nombresFichasEsperados == null) nombresFichasEsperados = List.of();
    }
}
