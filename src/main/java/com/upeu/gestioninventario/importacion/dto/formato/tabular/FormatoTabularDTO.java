package com.upeu.gestioninventario.importacion.dto.formato.tabular;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upeu.gestioninventario.importacion.dto.formato.common.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FormatoTabularDTO(
        String tipoFormato,
        String descripcion,

        HojasConfigDTO hojas,
        ColumnasEspecialesDTO columnasEspeciales,
        CeldasCombinadasDTO celdasCombinadas,
        FichasMultiplesDTO fichasMultiples,
        PreservarCoordenadasDTO preservarCoordenadas,
        VistaPreviaConfigDTO vistaPrevia,
        PaginacionConfigDTO paginacion,

        EncabezadosConfigDTO encabezados,
        DatosConfigDTO datos,
        RangoColumnasDTO rangoColumnas,
        ColumnasConfigDTO columnas
) {
    public FormatoTabularDTO {
        if (tipoFormato == null) tipoFormato = "TABULAR";
    }
}
