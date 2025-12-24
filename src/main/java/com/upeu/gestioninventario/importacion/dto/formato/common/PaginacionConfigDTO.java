package com.upeu.gestioninventario.importacion.dto.formato.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaginacionConfigDTO(
        Boolean habilitada,
        Integer elementosPorPaginaDefault,
        List<Integer> opcionesElementosPorPagina,
        Integer maxElementosSinPaginar,
        Boolean paginarPorFicha
) {
    public PaginacionConfigDTO {
        if (habilitada == null) habilitada = true;
        if (elementosPorPaginaDefault == null) elementosPorPaginaDefault = 50;
        if (opcionesElementosPorPagina == null) opcionesElementosPorPagina = List.of(25, 50, 100);
        if (maxElementosSinPaginar == null) maxElementosSinPaginar = 100;
        if (paginarPorFicha == null) paginarPorFicha = true;
    }
}
