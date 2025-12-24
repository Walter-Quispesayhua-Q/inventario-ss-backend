package com.upeu.gestioninventario.estructuras.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfiguracionAgrupacionDTO {

    private Boolean permitirMultiplesInstancias;
    private Boolean requiereTodosComponentes;
    private Integer minimoComponentes;
    private Boolean noAgruparSiEsIndividual;
}