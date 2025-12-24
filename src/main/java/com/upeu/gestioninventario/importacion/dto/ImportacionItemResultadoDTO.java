package com.upeu.gestioninventario.importacion.dto;

import com.upeu.gestioninventario.inventario.dto.bien.BienDTO;

public record ImportacionItemResultadoDTO(
        String identificador, // Identificador del ítem (ej. CAF o N° de Fila)
        boolean exitoso,
        String accion,
        String mensaje,
        BienDTO bien
) {
}
