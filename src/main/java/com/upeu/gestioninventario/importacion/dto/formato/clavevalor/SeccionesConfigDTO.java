package com.upeu.gestioninventario.importacion.dto.formato.clavevalor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SeccionesConfigDTO(
        Boolean habilitadas,
        Boolean detectarAutomatico,
        List<String> separadores,
        String patronSeccion
) {
    public SeccionesConfigDTO {
        if (habilitadas == null) habilitadas = true;
        if (detectarAutomatico == null) detectarAutomatico = true;
        if (separadores == null) separadores = List.of("===", "---", "***");
        if (patronSeccion == null) patronSeccion = "^\\[.*\\]$";
    }
}
