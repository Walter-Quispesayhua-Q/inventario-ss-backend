package com.upeu.gestioninventario.inventario.dto.bien;

import com.upeu.gestioninventario.inventario.dto.KeyValueInput;
import com.upeu.gestioninventario.ubicaciones.dto.UbicacionCreacionInput;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BienCreacionInput(

        Long categoriaId,

        String nombreNuevaCategoria,

        @NotNull(message = "La ubicación no puede ser nula")
        UbicacionCreacionInput ubicacion,

        @NotNull(message = "El ID del responsable no puede ser nulo")
        Long responsableActualId,

        @NotNull
        List<KeyValueInput> campos
) {
}