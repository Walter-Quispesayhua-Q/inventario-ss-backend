package com.upeu.gestioninventario.auditoria.repository;

import com.upeu.gestioninventario.auditoria.model.RegistroActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroActividadRepository extends JpaRepository<RegistroActividad, Long>, JpaSpecificationExecutor<RegistroActividad> {

    @Query("SELECT r FROM RegistroActividad r " +
            "LEFT JOIN FETCH r.usuario u " +
            "LEFT JOIN FETCH u.persona " +
            "WHERE r.usuario.id = :usuarioId " +
            "ORDER BY r.fechaOperacion DESC")
    List<RegistroActividad> findByUsuarioIdOrderByFechaOperacionDesc(@Param("usuarioId") Long usuarioId);

    @Query("SELECT r FROM RegistroActividad r " +
            "LEFT JOIN FETCH r.usuario u " +
            "LEFT JOIN FETCH u.persona " +
            "WHERE r.entidadAfectada = :entidadAfectada " +
            "AND r.entidadId = :entidadId " +
            "ORDER BY r.fechaOperacion DESC")
    List<RegistroActividad> findByEntidadAfectadaAndEntidadIdOrderByFechaOperacionDesc(
            @Param("entidadAfectada") String entidadAfectada,
            @Param("entidadId") Long entidadId
    );
}
