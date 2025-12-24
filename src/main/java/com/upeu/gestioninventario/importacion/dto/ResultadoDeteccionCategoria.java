package com.upeu.gestioninventario.importacion.dto;

import com.upeu.gestioninventario.categorias.dto.PlantillaCategoriaDTO;

/**
 * DTO que encapsula el resultado de la detección de categoría
 * con un cálculo dinámico de confianza basado en métricas reales
 */
public record ResultadoDeteccionCategoria(
        PlantillaCategoriaDTO plantilla,
        double confianza,
        int coincidenciasEncontradas,
        int totalPalabrasClave,
        double porcentajePalabrasClave,
        boolean esGenérica,

        String metodoDeteccion,  // PALABRAS_CLAVE, ML, APRENDIZAJE, HIBRIDO
        boolean usarCache
) {

}
