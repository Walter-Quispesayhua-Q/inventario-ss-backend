package com.upeu.gestioninventario.categorias.repository;

import com.upeu.gestioninventario.categorias.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long>, JpaSpecificationExecutor<Categoria> {
    Optional<Categoria> findByNombreCategoria(String nombreCategoria);

    Optional<Categoria> findByNombreCategoriaIgnoreCaseAndFechaEliminacionIsNull(String nombreCategoria);


    @Query("SELECT c FROM Categoria c " +
           "LEFT JOIN FETCH c.usuarioCreacion uc " +
           "LEFT JOIN FETCH uc.persona " +
           "LEFT JOIN FETCH c.usuarioUltimaModificacion uum " +
           "LEFT JOIN FETCH uum.persona " +
           "WHERE c.id = :id")
    Optional<Categoria> findByIdWithDetails(@Param("id") Long id);


    @Query("SELECT COUNT(b) FROM Bien b WHERE b.categoria.id = :categoriaId")
    Integer countBienesPorCategoria(@Param("categoriaId") Long categoriaId);

    long countByVisibleTrue();
}