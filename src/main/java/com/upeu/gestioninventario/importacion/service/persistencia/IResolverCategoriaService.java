package com.upeu.gestioninventario.importacion.service.persistencia;

import com.upeu.gestioninventario.categorias.model.Categoria;

import java.util.Optional;

public interface IResolverCategoriaService {

    Optional<Categoria> resolverPorNombre(String nombreCategoria);
}