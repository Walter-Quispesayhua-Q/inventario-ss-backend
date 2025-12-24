package com.upeu.gestioninventario.auditoria.repository.specifications;

import com.upeu.gestioninventario.auditoria.model.RegistroActividad;
import com.upeu.gestioninventario.auditoria.model.TipoOperacion;
import com.upeu.gestioninventario.shared.specifications.GenericSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class RegistroActividadSpecifications {

    public static Specification<RegistroActividad> buscarEnMultiplesCampos(String buscar) {
        return GenericSpecifications.searchInFields(
                List.of("descripcion", "entidadAfectada", "ipAddress"),
                buscar
        );
    }

    public static Specification<RegistroActividad> porUsuarioId(Long usuarioId) {
        return (root, query, cb) -> {
            if (usuarioId == null) {
                return null;
            }
            return cb.equal(root.get("usuario").get("id"), usuarioId);
        };
    }

    public static Specification<RegistroActividad> porTipoOperacion(String tipoOperacion) {
        return (root, query, cb) -> {
            if (tipoOperacion == null || tipoOperacion.isBlank()) {
                return null;
            }
            try {
                TipoOperacion tipo = TipoOperacion.valueOf(tipoOperacion);
                return cb.equal(root.get("tipoOperacion"), tipo);
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }

    public static Specification<RegistroActividad> porEntidadAfectada(String entidadAfectada) {
        return GenericSpecifications.fieldEquals("entidadAfectada", entidadAfectada);
    }

    public static Specification<RegistroActividad> porEntidadId(Long entidadId) {
        return (root, query, cb) -> {
            if (entidadId == null) {
                return null;
            }
            return cb.equal(root.get("entidadId"), entidadId);
        };
    }

    public static Specification<RegistroActividad> porRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        OffsetDateTime inicio = fechaInicio != null ? fechaInicio.atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime fin = fechaFin != null ? fechaFin.atOffset(ZoneOffset.UTC) : null;
        return porRangoFechas(inicio, fin);
    }

    public static Specification<RegistroActividad> porRangoFechas(OffsetDateTime fechaInicio, OffsetDateTime fechaFin) {
        return (root, query, cb) -> {
            if (fechaInicio == null && fechaFin == null) {
                return null;
            }
            if (fechaInicio != null && fechaFin != null) {
                return cb.between(root.get("fechaOperacion"), fechaInicio, fechaFin);
            }
            if (fechaInicio != null) {
                return cb.greaterThanOrEqualTo(root.get("fechaOperacion"), fechaInicio);
            }
            return cb.lessThanOrEqualTo(root.get("fechaOperacion"), fechaFin);
        };
    }

    public static Specification<RegistroActividad> soloActivosNoEliminados() {
        return GenericSpecifications.fieldIsNull("fechaEliminacion");
    }

    public static Specification<RegistroActividad> conUsuarioYPersona() {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("usuario", jakarta.persistence.criteria.JoinType.LEFT)
                    .fetch("persona", jakarta.persistence.criteria.JoinType.LEFT);
            }
            return null;
        };
    }
}
