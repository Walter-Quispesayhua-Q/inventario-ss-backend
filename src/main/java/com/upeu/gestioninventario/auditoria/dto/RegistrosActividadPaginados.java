package com.upeu.gestioninventario.auditoria.dto;

import com.upeu.gestioninventario.shared.dto.pagination.PaginacionInfo;
import java.util.List;

public record RegistrosActividadPaginados(
        List<RegistroActividadDTO> data,
        PaginacionInfo paginacion
) {
}
