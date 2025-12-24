package com.upeu.gestioninventario.estructuras.repository;

import com.upeu.gestioninventario.estructuras.model.Estacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstacionRepository extends JpaRepository<Estacion, Long> {

    long countByAmbienteIdAmbiente(Long idAmbiente);

    @Query(value = "SELECT MAX(CAST(REGEXP_REPLACE(SUBSTRING(e.codigo, 1, POSITION(' - (' IN e.codigo) - 1), '[^0-9]', '', 'g') AS INTEGER)) " +
                   "FROM estructuras.estaciones e " +
                   "WHERE e.id_ambiente = :idAmbiente " +
                   "AND e.codigo LIKE CONCAT(:prefijo, '-%') " +
                   "AND e.fecha_eliminacion IS NULL", nativeQuery = true)
    Optional<Integer> findMaxNumeroEstacionPorAmbienteYPrefijo(
            @Param("idAmbiente") Long idAmbiente,
            @Param("prefijo") String prefijo);


    @Query("SELECT e FROM Estacion e " +
           "JOIN FETCH e.ambiente a " +
           "JOIN FETCH a.piso p " +
           "JOIN FETCH p.edificio ed " +
           "JOIN FETCH e.tipoEstructura " +
           "WHERE a.idAmbiente = :idAmbiente " +
           "ORDER BY e.nombre")
    List<Estacion> findByAmbienteWithDetails(@Param("idAmbiente") Long idAmbiente);

    boolean existsByAmbienteIdAmbienteAndCodigo(Long idAmbiente, String codigo);

    @Query("SELECT e FROM Estacion e " +
           "WHERE e.capacidadBienes > " +
           "(SELECT COUNT(be) FROM BienEstacion be " +
           "WHERE be.estacion = e AND be.fechaDesasignacion IS NULL)")
    List<Estacion> findEstacionesConCapacidadDisponible();

    @Query("SELECT DISTINCT e FROM Estacion e " +
            "LEFT JOIN FETCH e.tipoEstructura " +
            "LEFT JOIN FETCH e.ambiente a " +
            "LEFT JOIN FETCH a.piso p " +
            "LEFT JOIN FETCH p.edificio " +
            "WHERE e.fechaEliminacion IS NULL " +
            "AND (:nombre IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS String), '%'))) " +
            "AND (:codigo IS NULL OR LOWER(e.codigo) LIKE LOWER(CONCAT('%', CAST(:codigo AS String), '%'))) " +
            "AND (:idAmbiente IS NULL OR e.ambiente.idAmbiente = :idAmbiente) " +
            "AND (:idPiso IS NULL OR e.ambiente.piso.idPiso = :idPiso) " +
            "AND (:idEdificio IS NULL OR e.ambiente.piso.edificio.idEdificio = :idEdificio) " +
            "AND (:idTipo IS NULL OR e.tipoEstructura.idTipo = :idTipo) " +
            "ORDER BY e.nombre")
    List<Estacion> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("codigo") String codigo,
            @Param("idAmbiente") Long idAmbiente,
            @Param("idPiso") Long idPiso,
            @Param("idEdificio") Long idEdificio,
            @Param("idTipo") Long idTipoEstructura);

    @Query("SELECT e FROM Estacion e " +
            "WHERE e.fechaEliminacion IS NULL " +
            "AND e.idEstacion IN (" +
            "  SELECT be.estacion.idEstacion FROM BienEstacion be " +
            "  WHERE be.fechaDesasignacion IS NULL " +
            "  GROUP BY be.estacion.idEstacion " +
            "  HAVING COUNT(be.id) > 0)")
    List<Estacion> findEstacionesConBienesAsignados();

    @Query("SELECT e FROM Estacion e " +
            "WHERE e.fechaEliminacion IS NULL " +
            "AND e.idEstacion NOT IN (" +
            "  SELECT DISTINCT be.estacion.idEstacion FROM BienEstacion be " +
            "  WHERE be.fechaDesasignacion IS NULL)")
    List<Estacion> findEstacionesSinBienesAsignados();

    @Query("SELECT a.idAmbiente, a.nombre, p.nombre, e.nombre, COUNT(est) " +
           "FROM Estacion est " +
           "JOIN est.ambiente a " +
           "JOIN a.piso p " +
           "JOIN p.edificio e " +
           "WHERE est.fechaEliminacion IS NULL " +
           "GROUP BY a.idAmbiente, a.nombre, p.nombre, e.nombre " +
           "ORDER BY e.nombre, p.nombre, a.nombre")
    List<Object[]> countEstacionesPorAmbiente();

    @Query("SELECT DISTINCT e FROM Estacion e " +
           "LEFT JOIN FETCH e.tipoEstructura " +
           "LEFT JOIN FETCH e.ambiente a " +
           "LEFT JOIN FETCH a.piso p " +
           "LEFT JOIN FETCH p.edificio " +
           "JOIN BienEstacion be ON be.estacion.idEstacion = e.idEstacion " +
           "JOIN be.bien b " +
           "WHERE e.fechaEliminacion IS NULL " +
           "AND be.fechaDesasignacion IS NULL " +
           "AND (LOWER(b.caf) LIKE LOWER(CONCAT('%', :codigoBien, '%')) " +
           "     OR LOWER(b.numeroSerie) LIKE LOWER(CONCAT('%', :codigoBien, '%'))) " +
           "ORDER BY e.nombre")
    List<Estacion> buscarPorCodigoBien(@Param("codigoBien") String codigoBien);
}
