package com.upeu.gestioninventario.estructuras.repository;

import com.upeu.gestioninventario.estructuras.model.Ambiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmbienteRepository extends JpaRepository<Ambiente, Long> {

    List<Ambiente> findByDepartamentoResponsableId(Long idDepartamento);

    long countByPisoIdPiso(Long idPiso);

    @Query("SELECT a FROM Ambiente a " +
           "JOIN FETCH a.piso p " +
           "JOIN FETCH p.edificio e " +
           "JOIN FETCH a.tipoEstructura " +
           "WHERE p.idPiso = :idPiso " +
           "ORDER BY a.nombre")
    List<Ambiente> findByPisoWithDetails(@Param("idPiso") Long idPiso);

    boolean existsByPisoIdPisoAndCodigo(Long idPiso, String codigo);


    @Query("SELECT a FROM Ambiente a " +
            "LEFT JOIN FETCH a.tipoEstructura " +
            "LEFT JOIN FETCH a.piso p " +
            "LEFT JOIN FETCH p.edificio " +
            "WHERE a.fechaEliminacion IS NULL " +
            "AND (:nombre IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS String), '%'))) " +
            "AND (:codigo IS NULL OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', CAST(:codigo AS String), '%'))) " +
            "AND (:idPiso IS NULL OR a.piso.idPiso = :idPiso) " +
            "AND (:idEdificio IS NULL OR a.piso.edificio.idEdificio = :idEdificio) " +
            "AND (:idTipo IS NULL OR a.tipoEstructura.idTipo = :idTipo) " +
            "AND (:idDepartamento IS NULL OR a.departamentoResponsable.id = :idDepartamento) " +
            "ORDER BY a.piso.edificio.nombre, a.piso.numeroPiso, a.nombre")
    List<Ambiente> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("codigo") String codigo,
            @Param("idPiso") Long idPiso,
            @Param("idEdificio") Long idEdificio,
            @Param("idTipo") Long idTipoEstructura,
            @Param("idDepartamento") Long idDepartamentoResponsable);
}
