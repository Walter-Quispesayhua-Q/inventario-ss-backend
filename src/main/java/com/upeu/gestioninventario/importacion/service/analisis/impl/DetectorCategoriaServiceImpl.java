package com.upeu.gestioninventario.importacion.service.analisis.impl;

import com.upeu.gestioninventario.importacion.service.analisis.util.MotorDeScoringService;
import com.upeu.gestioninventario.importacion.service.analisis.util.PuntuacionDetallada;
import com.upeu.gestioninventario.importacion.service.analisis.IDetectorCategoria;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import com.upeu.gestioninventario.categorias.dto.PlantillaCategoriaDTO;
import com.upeu.gestioninventario.importacion.dto.ResultadoDeteccionCategoria;
import com.upeu.gestioninventario.ml.service.PlantillaLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectorCategoriaServiceImpl implements IDetectorCategoria {

    private final PlantillaLoaderService plantillaLoaderService;
    private final MotorDeScoringService motorDeScoringService;

    @Override
    public ResultadoDeteccionCategoria detectarCategoria(String texto) {
        Collection<PlantillaSeedDTO> plantillasEnMemoria = plantillaLoaderService.getPlantillasCache().values();

        log.info("Iniciando deteccion de categoria: {} ...", texto.substring(0, Math.min(texto.length(), 50)));
        if (plantillasEnMemoria.isEmpty()) {
            log.error("No hay plantillas en la cache de PlantillaLoaderService para realizar la deteccion.");
            return null;
        }

        String textoNormalizado = normalizarTexto(texto);
        List<ResultadoConScore> resultadosConScore = new ArrayList<>();

        for (PlantillaSeedDTO plantilla : plantillasEnMemoria) {
            PuntuacionDetallada puntuacion = motorDeScoringService.calcularScoreParaPlantilla(plantilla, textoNormalizado);

            PlantillaCategoriaDTO dtoSimple = new PlantillaCategoriaDTO(
                    null,
                    plantilla.metadatos().idPlantilla(),
                    plantilla.metadatos().nombre(),
                    null, null, null, null, null, null, null, null, null, null, null, null, null
            );

            resultadosConScore.add(new ResultadoConScore(
                    dtoSimple,
                    puntuacion.getScoreFinal(),
                    puntuacion.getCoincidencias()
            ));
        }

        ResultadoConScore mejorResultado = resultadosConScore.stream()
                .max(Comparator.comparing(r -> r.score))
                .orElse(null);

        if (mejorResultado == null || mejorResultado.score <= 0.0) {
            return obtenerResultadoGenerico();
        }

        double confianza = calcularConfianza(mejorResultado.score);

        return new ResultadoDeteccionCategoria(
                mejorResultado.plantilla,
                confianza,
                mejorResultado.coincidencias,
                1,
                mejorResultado.score,
                false,
                "Detectado por scoring ML",
                false
        );
    }

    private static class ResultadoConScore {
        final PlantillaCategoriaDTO plantilla;
        final double score;
        final int coincidencias;

        ResultadoConScore(PlantillaCategoriaDTO plantilla, double score, int coincidencias) {
            this.plantilla = plantilla;
            this.score = score;
            this.coincidencias = coincidencias;
        }
    }

    private double calcularConfianza(double score) {
        if (score >= 5.0) return 0.99;
        if (score >= 3.0) return 0.95;
        if (score >= 2.0) return 0.90;
        if (score >= 1.5) return 0.85;
        if (score >= 1.0) return 0.80;
        if (score >= 0.5) return 0.75;
        return 0.70;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String textoNormalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return textoNormalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private ResultadoDeteccionCategoria obtenerResultadoGenerico() {
        return plantillaLoaderService.getPlantillasCache().values().stream()
                .filter(p -> p.metadatos().nombre().toLowerCase().contains("componentes y accesorios"))
                .findFirst()
                .map(generica -> {
                    PlantillaCategoriaDTO genericaDTO = new PlantillaCategoriaDTO(
                            null, generica.metadatos().idPlantilla(), generica.metadatos().nombre(),
                            null, null, null, null, null, null, null, null, null, null, null, null, null
                    );
                    return new ResultadoDeteccionCategoria(genericaDTO, 0.5, 0, 0, 0.0, true, "generica por defecto", true);
                })
                .orElseGet(() -> {
                    log.error("No se pudo encontrar una plantilla generica de respaldo.");
                    return null;
                });
    }
}
