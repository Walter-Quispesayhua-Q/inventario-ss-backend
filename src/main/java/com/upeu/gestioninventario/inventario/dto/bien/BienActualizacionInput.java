package com.upeu.gestioninventario.inventario.dto.bien;

import com.upeu.gestioninventario.inventario.dto.KeyValueInput;
import com.upeu.gestioninventario.ubicaciones.dto.UbicacionCreacionInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BienActualizacionInput(
        Long categoriaId,
        String nombreNuevaCategoria,
        @Valid UbicacionCreacionInput ubicacion,
        Long responsableActualId,
        @Valid @NotNull List<KeyValueInput> campos
) {
}