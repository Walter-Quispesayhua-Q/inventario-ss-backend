package com.upeu.gestioninventario.estructuras.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgrupacionesConfigDTO {

    private String version;
    private String descripcion;
    private List<AgrupacionDTO> agrupaciones;
    private PerifericosConfigDTO perifericos;
    private ReglasAgrupacionDTO reglas;
}