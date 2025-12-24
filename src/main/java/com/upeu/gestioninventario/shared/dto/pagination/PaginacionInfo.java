package com.upeu.gestioninventario.shared.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginacionInfo {
    private Integer paginaActual;
    private Integer totalPaginas;
    private Long totalElementos;
    private Integer elementosPorPagina;
    private Boolean tienePaginaSiguiente;
    private Boolean tienePaginaAnterior;

    public static PaginacionInfo from(int pageNumber, int totalPages, long totalElements, int pageSize) {
        return PaginacionInfo.builder()
                .paginaActual(pageNumber + 1)
                .totalPaginas(totalPages)
                .totalElementos(totalElements)
                .elementosPorPagina(pageSize)
                .tienePaginaSiguiente(pageNumber + 1 < totalPages)
                .tienePaginaAnterior(pageNumber > 0)
                .build();
    }
}
