package com.upeu.gestioninventario.categorias.service;

import com.upeu.gestioninventario.categorias.dto.*;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.util.List;

public interface ICategoriaService {

    OperacionResultadoDTO<CategoriaDTO> crearCategoria(CategoriaCreacionDTO formData);

    OperacionResultadoDTO<CategoriaDTO> actualizarCategoria(Long id, CategoriaActualizacionDTO updateData);

    OperacionResultadoDTO<Boolean> eliminarCategoria(Long id);

    CategoriasPaginadas obtenerCategoriasPaginadas(
            Integer page,
            Integer limit,
            Boolean estado,
            String ordenarPor,
            String orden,
            String buscar
    );

    List<PlantillaCategoriaDTO> sugerirPlantillas(String nombreCategoria);

    OperacionResultadoDTO<CategoriaDTO> aplicarPlantilla(Long categoriaId, Long plantillaId);

    List<CategoriaSimpleDTO> obtenerCategoriasSimples();
}