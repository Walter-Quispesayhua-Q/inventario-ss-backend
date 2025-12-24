package com.upeu.gestioninventario.importacion.dto.formato.tabular;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ColumnasConfigDTO(
        Boolean detectarTipo,
        List<DefinicionColumnaDTO> definiciones,
        DefinicionColumnaDTO porDefecto
) {
    public ColumnasConfigDTO {
        if (detectarTipo == null) detectarTipo = true;
        if (definiciones == null) definiciones = List.of();
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DefinicionColumnaDTO(
            String patron,
            String tipoDato,
            String formatoFecha,
            Boolean requerido
    ) {
        public DefinicionColumnaDTO {
            if (tipoDato == null) tipoDato = "texto";
            if (requerido == null) requerido = false;
        }
    }
}
