package com.upeu.gestioninventario.importacion.dto.agrupacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfiguracionAgrupacionesDTO {
    private String version;
    private String descripcion;
    private List<AgrupacionDTO> agrupaciones;
    private GrupoPerifericosPorDefectoDTO grupoPerifericosPorDefecto;
    private ReglasAgrupacionDTO reglas;
}

