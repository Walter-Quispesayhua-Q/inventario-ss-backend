package com.upeu.gestioninventario.shared.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Utilidades para construcción de Specifications de JPA de forma genérica.
 * <p>
 * Proporciona métodos estáticos para crear especificaciones comunes de filtrado,
 * búsqueda y comparación que pueden reutilizarse en cualquier repositorio.
 * <p>
 * Uso típico:
 * <pre>
 * Specification&lt;Entity&gt; spec = Specification.where(
 *     GenericSpecifications.fieldEquals("nombre", "valor")
 * ).and(
 *     GenericSpecifications.fieldLike("descripcion", "texto")
 * );
 * </pre>
 */
public class GenericSpecifications {

    private GenericSpecifications() {
        throw new UnsupportedOperationException("Clase de utilidades no instanciable");
    }

    public static <T> Specification<T> fieldEquals(String fieldName, Object value) {
        return (root, query, cb) -> 
            value == null ? null : cb.equal(root.get(fieldName), value);
    }

    public static <T> Specification<T> fieldLike(String fieldName, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            String pattern = "%" + value.toLowerCase() + "%";
            return cb.like(cb.lower(root.get(fieldName)), pattern);
        };
    }

    public static <T> Specification<T> searchInFields(List<String> fieldNames, String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty() || 
                fieldNames == null || fieldNames.isEmpty()) {
                return null;
            }
            
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                fieldNames.stream()
                    .map(fieldName -> cb.like(cb.lower(root.get(fieldName)), pattern))
                    .toArray(Predicate[]::new)
            );
        };
    }

    public static <T> Specification<T> fieldGreaterThan(String fieldName, Comparable<?> value) {
        return (root, query, cb) -> 
            value == null ? null : cb.greaterThan(root.get(fieldName), (Comparable) value);
    }

    public static <T> Specification<T> fieldLessThan(String fieldName, Comparable<?> value) {
        return (root, query, cb) -> 
            value == null ? null : cb.lessThan(root.get(fieldName), (Comparable) value);
    }

    public static <T> Specification<T> fieldBetween(String fieldName, Comparable<?> start, Comparable<?> end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return null;
            }
            if (start != null && end != null) {
                return cb.between(root.get(fieldName), (Comparable) start, (Comparable) end);
            }
            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get(fieldName), (Comparable) start);
            }
            return cb.lessThanOrEqualTo(root.get(fieldName), (Comparable) end);
        };
    }

    public static <T> Specification<T> fieldIn(String fieldName, List<?> values) {
        return (root, query, cb) -> 
            values == null || values.isEmpty() ? null : root.get(fieldName).in(values);
    }

    public static <T> Specification<T> fieldIsNull(String fieldName) {
        return (root, query, cb) -> cb.isNull(root.get(fieldName));
    }

    public static <T> Specification<T> fieldIsNotNull(String fieldName) {
        return (root, query, cb) -> cb.isNotNull(root.get(fieldName));
    }
}
