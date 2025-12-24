package com.upeu.gestioninventario.importacion.dto.formato.clavevalor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upeu.gestioninventario.importacion.dto.formato.common.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FormatoClaveValorDTO(
        String tipoFormato,
        String descripcion,

        HojasConfigDTO hojas,
        ColumnasEspecialesDTO columnasEspeciales,
        CeldasCombinadasDTO celdasCombinadas,
        FichasMultiplesDTO fichasMultiples,
        PreservarCoordenadasDTO preservarCoordenadas,
        VistaPreviaConfigDTO vistaPrevia,
        PaginacionConfigDTO paginacion,

        ClavesConfigDTO claves,
        ValoresConfigDTO valores,
        SeccionesConfigDTO secciones
) {
    public FormatoClaveValorDTO {
        if (tipoFormato == null) tipoFormato = "CLAVE_VALOR";
    }
}
