package com.upeu.gestioninventario.shared.utils;

import com.upeu.gestioninventario.shared.dto.pagination.PaginacionInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utilidades para manejo de paginación en consultas Spring Data JPA.
 * <p>
 * Proporciona métodos para crear objetos Pageable y PaginacionInfo
 * con valores normalizados y límites de seguridad.
 */
public final class PaginacionUtils {

    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 100;

    private PaginacionUtils() {
        throw new UnsupportedOperationException("Clase de utilidades no instanciable");
    }

    /**
     * Crea un Pageable con valores normalizados para paginación y ordenamiento.
     * 
     * @param page Número de página (1-indexed), se convierte a 0-indexed
     * @param limit Tamaño de página, limitado a MAX_PAGE_SIZE
     * @param ordenarPor Campo por el cual ordenar
     * @param orden Dirección del ordenamiento (ASC/DESC)
     * @return Pageable configurado
     */
    public static Pageable createPageable(Integer page, Integer limit, String ordenarPor, String orden) {
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = normalizePageSize(limit);
        Sort sort = createSort(ordenarPor, orden);
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    /**
     * Normaliza el tamaño de página aplicando límites de seguridad.
     * Usa DEFAULT_PAGE_SIZE si el valor es inválido, limita a MAX_PAGE_SIZE si excede.
     */
    public static int normalizePageSize(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(limit, MAX_PAGE_SIZE);
    }

    /**
     * Crea un Sort basado en campo y dirección.
     * Retorna Sort.unsorted() si no se especifica campo.
     */
    public static Sort createSort(String sortField, String direction) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return Sort.unsorted();
        }
        
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        return Sort.by(sortDirection, sortField);
    }

    /**
     * Crea un objeto PaginacionInfo desde un Page de Spring Data.
     */
    public static <T> PaginacionInfo createPaginacionInfo(Page<T> page) {
        return PaginacionInfo.from(
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getSize()
        );
    }
}
