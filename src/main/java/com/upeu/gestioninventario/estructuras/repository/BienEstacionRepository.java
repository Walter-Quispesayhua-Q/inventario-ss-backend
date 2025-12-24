package com.upeu.gestioninventario.estructuras.repository;

import com.upeu.gestioninventario.estructuras.model.BienEstacion;
import com.upeu.gestioninventario.inventario.model.Bien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BienEstacionRepository extends JpaRepository<BienEstacion, Long> {

    @Query("SELECT be FROM BienEstacion be WHERE be.bien.id = :idBien AND be.fechaDesasignacion IS NULL")
    Optional<BienEstacion> findByBienIdAndFechaDesasignacionIsNull(@Param("idBien") Long idBien);

    @Query("SELECT be FROM BienEstacion be WHERE be.estacion.idEstacion = :idEstacion AND be.fechaDesasignacion IS NULL")
    List<BienEstacion> findByEstacionIdAndFechaDesasignacionIsNull(@Param("idEstacion") Long idEstacion);

    @Query("SELECT COUNT(be) FROM BienEstacion be WHERE be.estacion.idEstacion = :idEstacion AND be.fechaDesasignacion IS NULL")
    long countByEstacionIdAndFechaDesasignacionIsNull(@Param("idEstacion") Long idEstacion);

    @Query("SELECT be FROM BienEstacion be WHERE be.bien.id = :idBien ORDER BY be.fechaAsignacion DESC")
    List<BienEstacion> findByBienIdOrderByFechaAsignacionDesc(@Param("idBien") Long idBien);


    @Query("SELECT be FROM BienEstacion be " +
           "JOIN FETCH be.bien b " +
           "WHERE be.estacion.idEstacion = :idEstacion " +
           "AND (:soloActivos = false OR be.fechaDesasignacion IS NULL) " +
           "ORDER BY be.fechaAsignacion DESC")
    Page<BienEstacion> findByEstacionIdEstacionAndAsignacionActiva(
        @Param("idEstacion") Long idEstacion, 
        @Param("soloActivos") Boolean soloActivos, 
        Pageable pageable);

    @Query("SELECT b FROM Bien b " +
           "LEFT JOIN FETCH b.categoria " +
           "LEFT JOIN FETCH b.responsableActual " +
           "LEFT JOIN FETCH b.atributos ba " +
           "LEFT JOIN FETCH ba.tipoAtributo " +
           "WHERE NOT EXISTS (" +
           "    SELECT 1 FROM BienEstacion be " +
           "    WHERE be.bien = b AND be.fechaDesasignacion IS NULL" +
           ") " +
           "ORDER BY b.fechaCreacion DESC")
    List<Bien> findBienesLibres();

    @Query("SELECT be FROM BienEstacion be " +
           "JOIN FETCH be.bien b " +
           "JOIN FETCH be.estacion e " +
           "LEFT JOIN FETCH b.categoria " +
           "LEFT JOIN FETCH b.responsableActual " +
           "LEFT JOIN FETCH b.atributos ba " +
           "LEFT JOIN FETCH ba.tipoAtributo " +
           "WHERE be.fechaDesasignacion IS NULL " +
           "AND (:idEstacionExcluir IS NULL OR e.idEstacion <> :idEstacionExcluir) " +
           "ORDER BY e.nombre, b.nombreBien")
    List<BienEstacion> findBienesAsignadosExcluyendoEstacion(@Param("idEstacionExcluir") Long idEstacionExcluir);

    @Query("SELECT be FROM BienEstacion be " +
           "JOIN FETCH be.bien b " +
           "LEFT JOIN FETCH b.categoria " +
           "LEFT JOIN FETCH b.responsableActual " +
           "LEFT JOIN FETCH b.atributos ba " +
           "LEFT JOIN FETCH ba.tipoAtributo " +
           "WHERE be.estacion.idEstacion = :idEstacion " +
           "AND be.fechaDesasignacion IS NULL " +
           "AND be.componente IS NULL " +
           "ORDER BY b.nombreBien")
    List<BienEstacion> findBienesIndividualesPorEstacion(@Param("idEstacion") Long idEstacion);
}
