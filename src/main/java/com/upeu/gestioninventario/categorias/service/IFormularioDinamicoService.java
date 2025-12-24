package com.upeu.gestioninventario.categorias.service;

import com.upeu.gestioninventario.inventario.dto.AtributoFormularioDTO;
import java.util.List;

public interface IFormularioDinamicoService {

    List<AtributoFormularioDTO> obtenerAtributosParaCategoria(Long categoriaId, String buscarNombrePlantilla);
}