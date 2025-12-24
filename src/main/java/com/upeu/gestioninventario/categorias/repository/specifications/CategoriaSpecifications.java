package com.upeu.gestioninventario.categorias.repository.specifications;

import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.shared.specifications.GenericSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class CategoriaSpecifications {

    public static Specification<Categoria> conEstado(Boolean estado) {
        return GenericSpecifications.fieldEquals("estado", estado);
    }

    public static Specification<Categoria> buscarPorNombreODescripcion(String buscar) {
        return GenericSpecifications.searchInFields(
            List.of("nombreCategoria", "descripcion"), 
            buscar
        );
    }

    public static Specification<Categoria> noEliminado() {
        return GenericSpecifications.fieldIsNull("fechaEliminacion");
    }

    public static Specification<Categoria> soloVisibles() {
        return GenericSpecifications.fieldEquals("visible", true);
    }
}
