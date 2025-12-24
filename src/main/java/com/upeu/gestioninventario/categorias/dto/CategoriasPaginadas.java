package com.upeu.gestioninventario.categorias.dto;

import com.upeu.gestioninventario.shared.dto.pagination.PaginacionInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriasPaginadas {
    private List<CategoriaDTO> data;
    private PaginacionInfo paginacion;

    public static CategoriasPaginadas of(List<CategoriaDTO> data, PaginacionInfo paginacion) {
        return CategoriasPaginadas.builder()
                .data(data)
                .paginacion(paginacion)
                .build();
    }
}
