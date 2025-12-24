package com.upeu.gestioninventario.importacion.dto.agrupacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfiguracionAgrupacionDetalleDTO {
    private boolean permitirMultiplesInstancias;
    private boolean requiereTodosComponentes;
    private int minimoComponentes;
    private boolean noAgruparSiEsIndividual;
}
