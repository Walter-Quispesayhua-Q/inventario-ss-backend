package com.upeu.gestioninventario.estructuras.repository;

import com.upeu.gestioninventario.estructuras.model.Edificio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EdificioRepository extends JpaRepository<Edificio, Long> {

    Optional<Edificio> findByCodigo(String codigo);

    @Query("SELECT COUNT(e) FROM Edificio e WHERE e.fechaEliminacion IS NULL")
    long countEdificiosActivos();

    @Query("SELECT DISTINCT e FROM Edificio e " +
           "LEFT JOIN FETCH e.tipoEstructura " +
           "WHERE e.fechaEliminacion IS NULL " +
           "ORDER BY e.nombre")
    List<Edificio> findAllWithTipoEstructura();

    boolean existsByCodigo(String codigo);

    @Query("SELECT e FROM Edificio e " +
            "LEFT JOIN FETCH e.tipoEstructura " +
            "WHERE e.fechaEliminacion IS NULL " +
            "AND (:nombre IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS String), '%'))) " +
            "AND (:codigo IS NULL OR LOWER(e.codigo) LIKE LOWER(CONCAT('%', CAST(:codigo AS String), '%'))) " +
            "AND (:idTipo IS NULL OR e.tipoEstructura.idTipo = :idTipo) " +
            "AND (:idResponsable IS NULL OR e.responsableMantenimiento.id = :idResponsable) " +
            "ORDER BY e.nombre")
    List<Edificio> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("codigo") String codigo,
            @Param("idTipo") Long idTipoEstructura,
            @Param("idResponsable") Long idResponsableMantenimiento);
}
