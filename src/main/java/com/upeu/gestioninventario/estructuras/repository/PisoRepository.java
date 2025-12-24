package com.upeu.gestioninventario.estructuras.repository;

import com.upeu.gestioninventario.estructuras.model.Piso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PisoRepository extends JpaRepository<Piso, Long> {

    Optional<Piso> findByEdificioIdEdificioAndNumeroPiso(Long idEdificio, Integer numeroPiso);

    long countByEdificioIdEdificio(Long idEdificio);

    @Query("SELECT p FROM Piso p " +
           "JOIN FETCH p.edificio e " +
           "JOIN FETCH p.tipoEstructura " +
           "WHERE e.idEdificio = :idEdificio " +
           "ORDER BY p.numeroPiso")
    List<Piso> findByEdificioWithDetails(@Param("idEdificio") Long idEdificio);

    boolean existsByEdificioIdEdificioAndCodigo(Long idEdificio, String codigo);

    boolean existsByEdificioIdEdificioAndNumeroPiso(Long idEdificio, Integer numeroPiso);

    @Query("SELECT p FROM Piso p " +
            "LEFT JOIN FETCH p.tipoEstructura " +
            "LEFT JOIN FETCH p.edificio " +
            "WHERE p.fechaEliminacion IS NULL " +
            "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS String), '%'))) " +
            "AND (:codigo IS NULL OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', CAST(:codigo AS String), '%'))) " +
            "AND (:idEdificio IS NULL OR p.edificio.idEdificio = :idEdificio) " +
            "AND (:numeroPiso IS NULL OR p.numeroPiso = :numeroPiso) " +
            "AND (:idTipo IS NULL OR p.tipoEstructura.idTipo = :idTipo) " +
            "ORDER BY p.edificio.nombre, p.numeroPiso")
    List<Piso> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("codigo") String codigo,
            @Param("idEdificio") Long idEdificio,
            @Param("numeroPiso") Integer numeroPiso,
            @Param("idTipo") Long idTipoEstructura);
}
