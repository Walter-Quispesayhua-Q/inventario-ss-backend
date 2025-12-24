package com.upeu.gestioninventario.importacion.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReconocimientoConfigDTO(
    @JsonProperty("palabrasClave") Map<String, Map<String, Object>> palabrasClave,
    @JsonProperty("palabrasTecnicas") Map<String, List<String>> palabrasTecnicas,
    @JsonProperty("marcas") Map<String, List<String>> marcas,
    @JsonProperty("palabrasExclusion") Map<String, List<String>> palabrasExclusion
) {}