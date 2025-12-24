package com.upeu.gestioninventario.importacion.dto;

import java.util.Map;

/**
 * DTO para representar un componente interno de una PC
 */
public record ComponenteInternoDTO(
        String tipoComponente,
        String marca,
        String modelo,
        String capacidadVelocidad,
        String tipo,
        String serie,
        Map<String, String> atributosAdicionales
) {
    
    public static ComponenteInternoDTO crear(
            String tipoComponente,
            String marca,
            String modelo,
            String capacidadVelocidad,
            String tipo,
            String serie) {
        return new ComponenteInternoDTO(
                tipoComponente,
                marca,
                modelo,
                capacidadVelocidad,
                tipo,
                serie,
                Map.of()
        );
    }
}
