package com.upeu.gestioninventario.importacion.dto;


import com.upeu.gestioninventario.estructuras.dto.tipo.TipoComponenteEnum;

import java.util.List;
import java.util.Map;

public record FilaPreviewDTO(
        int numeroFila,
        Map<String, String> valoresOriginales,
        Map<String, String> atributosExtraidos,
        boolean esValida,
        List<String> advertencias,
        String categoriaDetectada,
        double confianzaCategoria,
        AmbienteDetectadoDTO ambienteDetectado,
        String idEstacionAsociada,
        String idComponenteAsociado,
        TipoComponenteEnum tipoComponente,
        List<ComponenteInternoDTO> componentesInternos,
        String idGrupo,
        String nombreGrupo
) {

    public FilaPreviewDTO conEstacion(String idEstacion, String idComponente, TipoComponenteEnum tipo) {
        return new FilaPreviewDTO(
                numeroFila, valoresOriginales, atributosExtraidos, esValida,
                advertencias, categoriaDetectada, confianzaCategoria,
                ambienteDetectado, idEstacion, idComponente, tipo, componentesInternos,
                idGrupo, nombreGrupo
        );
    }
    
    public FilaPreviewDTO conGrupo(String nuevoIdGrupo, String nuevoNombreGrupo) {
        return new FilaPreviewDTO(
                numeroFila, valoresOriginales, atributosExtraidos, esValida,
                advertencias, categoriaDetectada, confianzaCategoria,
                ambienteDetectado, idEstacionAsociada, idComponenteAsociado, tipoComponente,
                componentesInternos, nuevoIdGrupo, nuevoNombreGrupo
        );
    }

    public boolean tieneEstacionAsociada() {
        return idEstacionAsociada != null;
    }
}

