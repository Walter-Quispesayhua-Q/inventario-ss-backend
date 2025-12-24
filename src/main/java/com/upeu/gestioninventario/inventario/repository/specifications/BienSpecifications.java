package com.upeu.gestioninventario.inventario.repository.specifications;

import com.upeu.gestioninventario.inventario.model.Bien;
import com.upeu.gestioninventario.shared.specifications.GenericSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class BienSpecifications {

    public static Specification<Bien> buscarEnMultiplesCampos(String buscar) {
        return GenericSpecifications.searchInFields(
            List.of("nombreBien", "caf", "numeroSerie", "observaciones"),
            buscar
        );
    }

    public static Specification<Bien> porCategoriaId(Long categoriaId) {
        return (root, query, cb) -> {
            if (categoriaId == null) {
                return null;
            }
            return cb.equal(root.get("categoria").get("id"), categoriaId);
        };
    }

    public static Specification<Bien> porUbicacionId(Long ubicacionId) {
        return (root, query, cb) -> {
            if (ubicacionId == null) {
                return null;
            }
            return cb.equal(root.get("ubicacionActual").get("id"), ubicacionId);
        };
    }

    public static Specification<Bien> porEstadoOperacional(String estadoOperacional) {
        return GenericSpecifications.fieldEquals("estadoOperacional", estadoOperacional);
    }

    public static Specification<Bien> porEstadoFisico(String estadoFisico) {
        return GenericSpecifications.fieldEquals("estadoFisico", estadoFisico);
    }

    public static Specification<Bien> porDepartamentoId(Long departamentoId) {
        return (root, query, cb) -> {
            if (departamentoId == null) {
                return null;
            }
            return cb.equal(root.get("departamento").get("id"), departamentoId);
        };
    }

    public static Specification<Bien> porResponsableId(Long responsableId) {
        return (root, query, cb) -> {
            if (responsableId == null) {
                return null;
            }
            return cb.equal(root.get("responsableActual").get("id"), responsableId);
        };
    }

    public static Specification<Bien> soloActivosNoEliminados() {
        return GenericSpecifications.fieldIsNull("fechaEliminacion");
    }

}
