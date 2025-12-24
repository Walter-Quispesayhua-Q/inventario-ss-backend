package com.upeu.gestioninventario.importacion.service.analisis;

import com.upeu.gestioninventario.importacion.dto.ResultadoDeteccionCategoria;

public interface IDetectorCategoria {

    ResultadoDeteccionCategoria detectarCategoria(String texto);
}
