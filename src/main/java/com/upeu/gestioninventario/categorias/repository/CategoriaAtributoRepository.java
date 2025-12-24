package com.upeu.gestioninventario.categorias.repository;

import com.upeu.gestioninventario.categorias.model.CategoriaAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaAtributoRepository extends JpaRepository<CategoriaAtributo, Long> {
    List<CategoriaAtributo> findByCategoriaId(Long categoriaId);
}