package com.upeu.gestioninventario.inventario.repository;

import com.upeu.gestioninventario.inventario.model.Bien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BienRepository extends JpaRepository<Bien, Long>, JpaSpecificationExecutor<Bien> {
    Optional<Bien> findByCaf(String caf);
    Optional<Bien> findByNumeroSerie(String numeroSerie);
    boolean existsByCategoriaId(Long categoriaId);

    List<Bien> findByResponsableActualId(Long responsableId);
    long countByResponsableActualId(Long responsableId);

    @Query("SELECT b FROM Bien b " +
           "LEFT JOIN FETCH b.categoria " +
           "LEFT JOIN FETCH b.ubicacionActual ua " +
           "LEFT JOIN FETCH b.responsableActual " +
           "LEFT JOIN FETCH b.usuarioCreacion uc LEFT JOIN FETCH uc.persona " +
           "LEFT JOIN FETCH b.usuarioUltimaModificacion uum LEFT JOIN FETCH uum.persona " +
           "LEFT JOIN FETCH b.departamento " +
           "LEFT JOIN FETCH b.atributos ba " +
           "LEFT JOIN FETCH ba.tipoAtributo " +
           "WHERE b.id = :id")
    Optional<Bien> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT b FROM Bien b " +
           "LEFT JOIN FETCH b.categoria c " +
           "LEFT JOIN FETCH b.ubicacionActual " +
           "LEFT JOIN FETCH b.responsableActual " +
           "LEFT JOIN FETCH b.usuarioCreacion uc LEFT JOIN FETCH uc.persona " +
           "LEFT JOIN FETCH b.usuarioUltimaModificacion uum LEFT JOIN FETCH uum.persona " +
           "LEFT JOIN FETCH b.departamento " +
           "LEFT JOIN FETCH b.atributos ba " +
           "LEFT JOIN FETCH ba.tipoAtributo " +
           "WHERE (LOWER(b.nombreBien) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(b.caf) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(b.numeroSerie) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(b.observaciones) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "AND NOT EXISTS (" +
           "    SELECT 1 FROM BienEstacion be " +
           "    JOIN be.componente ce " +
           "    WHERE be.bien = b AND ce.tipo = 'EQUIPO_COMPLETO'" +
           ") " +
           "ORDER BY b.fechaCreacion DESC")
    List<Bien> buscarBienesIndividuales(@Param("termino") String termino);

    @Query("SELECT DISTINCT b FROM Bien b " +
           "LEFT JOIN FETCH b.categoria c " +
           "LEFT JOIN FETCH b.ubicacionActual " +
           "LEFT JOIN FETCH b.responsableActual " +
           "LEFT JOIN FETCH b.usuarioCreacion uc LEFT JOIN FETCH uc.persona " +
           "LEFT JOIN FETCH b.usuarioUltimaModificacion uum LEFT JOIN FETCH uum.persona " +
           "LEFT JOIN FETCH b.departamento " +
           "LEFT JOIN FETCH b.atributos ba " +
           "LEFT JOIN FETCH ba.tipoAtributo " +
           "WHERE c.id = :categoriaId " +
           "AND NOT EXISTS (" +
           "    SELECT 1 FROM BienEstacion be " +
           "    JOIN be.componente ce " +
           "    WHERE be.bien = b AND ce.tipo = 'EQUIPO_COMPLETO'" +
           ") " +
           "ORDER BY b.fechaCreacion DESC")
    List<Bien> findBienesIndividualesByCategoriaId(@Param("categoriaId") Long categoriaId);


    @Query("SELECT c.id, c.nombreCategoria, COUNT(b) " +
           "FROM Bien b JOIN b.categoria c " +
           "GROUP BY c.id, c.nombreCategoria " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> countBienesPorCategoriaAgrupado();

    @Query("SELECT b FROM Bien b " +
           "LEFT JOIN FETCH b.categoria " +
           "LEFT JOIN FETCH b.ubicacionActual " +
           "ORDER BY b.fechaCreacion DESC " +
           "LIMIT 10")
    List<Bien> findTop10ByOrderByFechaCreacionDesc();
}