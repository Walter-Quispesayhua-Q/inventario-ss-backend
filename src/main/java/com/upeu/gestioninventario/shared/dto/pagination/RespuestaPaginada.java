package com.upeu.gestioninventario.shared.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaPaginada<T> {
    private List<T> data;
    private PaginacionInfo paginacion;

    public static <T> RespuestaPaginada<T> of(List<T> data, PaginacionInfo paginacion) {
        return RespuestaPaginada.<T>builder()
                .data(data)
                .paginacion(paginacion)
                .build();
    }
}
