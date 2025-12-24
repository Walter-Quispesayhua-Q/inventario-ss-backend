package com.upeu.gestioninventario.importacion.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EstrategiaExtraccionDTO(
        String tipo,
        List<String> nombresColumna,
        String patron,
        int grupo,
        boolean multiple,
        List<String> lista,
        String fuente
) {}