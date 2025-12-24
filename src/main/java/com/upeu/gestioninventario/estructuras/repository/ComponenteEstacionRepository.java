package com.upeu.gestioninventario.estructuras.repository;

import com.upeu.gestioninventario.estructuras.model.ComponenteEstacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponenteEstacionRepository extends JpaRepository<ComponenteEstacion, Long> {

    boolean existsByEstacionIdEstacionAndNombre(Long idEstacion, String nombre);


    @Query("SELECT c FROM ComponenteEstacion c " +
           "LEFT JOIN FETCH c.bienesAsignados ba " +
           "LEFT JOIN FETCH ba.bien b " +
           "LEFT JOIN FETCH b.categoria " +
           "LEFT JOIN FETCH b.responsableActual " +
           "LEFT JOIN FETCH b.atributos bat " +
           "LEFT JOIN FETCH bat.tipoAtributo " +
           "WHERE c.estacion.idEstacion = :idEstacion " +
           "ORDER BY c.orden ASC")
    List<ComponenteEstacion> findByEstacionWithBienes(@Param("idEstacion") Long idEstacion);

    @Query("SELECT c FROM ComponenteEstacion c " +
           "LEFT JOIN FETCH c.bienesAsignados ba " +
           "LEFT JOIN FETCH ba.bien b " +
           "LEFT JOIN FETCH b.categoria " +
           "LEFT JOIN FETCH b.responsableActual " +
           "LEFT JOIN FETCH b.atributos bat " +
           "LEFT JOIN FETCH bat.tipoAtributo " +
           "WHERE c.idComponente = :idComponente")
    Optional<ComponenteEstacion> findByIdWithBienes(@Param("idComponente") Long idComponente);

    @Query("SELECT COALESCE(MAX(c.orden), 0) FROM ComponenteEstacion c WHERE c.estacion.idEstacion = :idEstacion")
    Integer findMaxOrdenByEstacion(@Param("idEstacion") Long idEstacion);

}
