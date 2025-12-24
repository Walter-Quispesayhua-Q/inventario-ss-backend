package com.upeu.gestioninventario.importacion.dto.formato.respuesta;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;

import java.util.List;
import java.util.Map;

public record MetadataDetectadaDTO(
        Map<String, TituloDetectadoDTO> titulosDetectados,
        List<FichaDetectadaDTO> fichasMultiples,
        List<SeccionDetectadaDTO> secciones,
        Map<String, List<Integer>> agrupacionesInteligentes,
        List<EstacionConComponentesDTO> estacionesSugeridas
) {

    public record TituloDetectadoDTO(
            String clave,
            String titulo,
            String valor,
            String coordenada,
            boolean esFicha,
            int puntuacion
    ) {}

    public record FichaDetectadaDTO(
            String id,
            int filaInicio,
            int filaFin,
            Map<String, String> atributos,
            String categoriaDetectada,
            EstacionConComponentesDTO estacionSugerida
    ) {}

    public record SeccionDetectadaDTO(
            String nombre,
            int filaInicio,
            int filaFin,
            String separador
    ) {}

    public int totalBienes() {
        if (estacionesSugeridas == null) return 0;
        return estacionesSugeridas.stream()
                .mapToInt(EstacionConComponentesDTO::getTotalBienes)
                .sum();
    }
}
