package com.upeu.gestioninventario.inventario.dto.bien;

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
public class BienesPaginados {
    private List<BienDTO> data;
    private PaginacionInfo paginacion;

    public static BienesPaginados of(List<BienDTO> data, PaginacionInfo paginacion) {
        return BienesPaginados.builder()
                .data(data)
                .paginacion(paginacion)
                .build();
    }
}
