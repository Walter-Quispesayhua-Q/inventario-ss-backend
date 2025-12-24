package com.upeu.gestioninventario.importacion.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtraccionConfigDTO(
        boolean priorizarCampoEstatico,
        List<EstrategiaExtraccionDTO> estrategias
) {}