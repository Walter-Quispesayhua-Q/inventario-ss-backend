package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upeu.gestioninventario.importacion.dto.formato.common.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FormatoMatrizDTO(
        String tipoFormato,
        String descripcion,
        HojasConfigDTO hojas,
        CeldasCombinadasDTO celdasCombinadas,
        FichasMultiplesDTO fichasMultiples,
        PreservarCoordenadasDTO preservarCoordenadas,
        VistaPreviaConfigDTO vistaPrevia,
        PaginacionConfigDTO paginacion,
        EncabezadosColumnasDTO encabezadosColumnas,
        EncabezadosFilasDTO encabezadosFilas,
        DatosMatrizDTO datos,
        AgrupacionInteligenteDTO agrupacionInteligente,
        DeteccionFichaDTO deteccionFicha,
        EstructuraFichaDTO estructuraFicha,
        HeuristicasDTO heuristicas,
        MapeoAtributosDTO mapeoAtributos,
        SalidaConfigDTO salida
) {
    public FormatoMatrizDTO {
        if (tipoFormato == null) tipoFormato = "MATRIZ";
    }
}