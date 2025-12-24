package com.upeu.gestioninventario.importacion.service.analisis;

import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;

import java.util.Map;

public interface IExtractorAtributos {

    Map<String, String> extraerAtributos(
            String textoConcatenado,
            Map<String, String> datosPorColumna,
            PlantillaSeedDTO plantilla
    );
}
