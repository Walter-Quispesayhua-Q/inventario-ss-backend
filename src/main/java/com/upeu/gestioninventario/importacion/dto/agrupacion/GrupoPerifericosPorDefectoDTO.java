package com.upeu.gestioninventario.importacion.dto.agrupacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GrupoPerifericosPorDefectoDTO {
    private String id;
    private String nombreComponente;
    private String descripcion;
}
