package com.upeu.gestioninventario.importacion.service.analisis.util;

import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import com.upeu.gestioninventario.importacion.dto.internal.BonusRuleDTO;
import com.upeu.gestioninventario.importacion.dto.internal.ReconocimientoConfigDTO;
import com.upeu.gestioninventario.importacion.dto.internal.ScoringConfigDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotorDeScoringService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PuntuacionDetallada calcularScoreParaPlantilla(PlantillaSeedDTO plantilla, String textoNormalizado) {
        ScoringConfigDTO scoringConfig = objectMapper.convertValue(plantilla.scoring(), ScoringConfigDTO.class);
        ReconocimientoConfigDTO reconocimientoConfig =  objectMapper.convertValue(plantilla.reconocimiento(), ReconocimientoConfigDTO.class);
        Map<String, Map<String, String>> patronesConfig = objectMapper.convertValue(plantilla.patronesRegex(), new TypeReference<>() {});

        if (scoringConfig == null || reconocimientoConfig == null || patronesConfig == null ) {
            log.warn("La plantilla '{}' no tiene configuracion de scoring o reconocimiento.", plantilla.metadatos().nombre());
            return new PuntuacionDetallada(0.0, 0);
        }

        AtomicInteger coincidencias = new AtomicInteger(0);

        double scorePalabrasClave = calcularScorePalabrasClave(reconocimientoConfig.palabrasClave(), textoNormalizado, coincidencias);
        double scorePalabrasTecnicas = calcularScorePalabrasTecnicas(reconocimientoConfig.palabrasTecnicas(), textoNormalizado, coincidencias);
        double scorePatrones = calcularScorePatrones(patronesConfig, textoNormalizado, coincidencias);
        double penalizacionExclusion = calcularPenalizacionExclusion(reconocimientoConfig.palabrasExclusion(), textoNormalizado, coincidencias);

        if (penalizacionExclusion <= -999) {
            log.debug("Exclusion absoluta encontrada para plantilla '{}' . Score final: 0", plantilla.metadatos().nombre());
            return new PuntuacionDetallada(0.0, coincidencias.get());
        }

        double scorePonderado =
                (scorePalabrasClave * scoringConfig.pesos().getOrDefault("palabrasClave", 0.0)) +
                        (scorePalabrasTecnicas * scoringConfig.pesos().getOrDefault("palabrasTecnicas", 0.0)) +
                        (scorePatrones * scoringConfig.pesos().getOrDefault("patrones", 0.0));

        double scoreBonificaciones = calcularBonificaciones(scoringConfig.bonificaciones(), textoNormalizado, coincidencias);
        double scoreFinal = scorePonderado + penalizacionExclusion + scoreBonificaciones;

        log.debug("Score para '{}': {} (Ponderado) + {} (Penalización) + {} (Bonus) = {}", plantilla.metadatos().nombre(), scorePonderado, penalizacionExclusion, scoreBonificaciones, scoreFinal);
        return new PuntuacionDetallada(Math.max(0, scoreFinal), coincidencias.get());
    }

    private double calcularScorePalabrasClave(Map<String, Map<String, Object>> config, String texto, AtomicInteger coincidencias) {
        double score = 0.0;
        if (config == null) return 0.0;

        for (Map.Entry<String, Map<String, Object>> nivel : config.entrySet()) {
            Map<String, Object> detallesNivel = nivel.getValue();
            List<String> terminos =  (List<String>) detallesNivel.get("terminos");
            double peso = ((Number) detallesNivel.getOrDefault("peso", 0.0)).doubleValue();

            for (String termino : terminos) {
                Pattern pattern = Pattern.compile(".*\\b" + Pattern.quote(termino) + "\\b.*");
                if (pattern.matcher(texto).matches()) {
                    score += peso;
                    coincidencias.incrementAndGet();
                }
            }
        }
        return score;
    }

    private double calcularScorePalabrasTecnicas(Map<String, List<String>> config, String texto, AtomicInteger coincidencias) {
        double score = 0.0;
        if (config == null) return 0.0;

        for (List<String> terminos : config.values()) {
            for (String termino : terminos) {
                Pattern pattern = Pattern.compile(".*\\b" + Pattern.quote(termino) + "\\b.*");
                if (pattern.matcher(texto).matches()) {
                    score += 1.0;
                    coincidencias.incrementAndGet();
                }
            }
        }
        return score;
    }

    private double calcularPenalizacionExclusion(Map<String, List<String>> config, String texto, AtomicInteger coincidencias) {
        if (config == null) return 0.0;

        for (String termino : config.getOrDefault("absoluta", List.of())) {
            Pattern pattern = Pattern.compile(".*\\b" + Pattern.quote(termino) + "\\b.*");
            if (pattern.matcher(texto).matches()) {
                return -1000.0;
            }
        }
        double penalizacionExclusion = 0.0;
        long fuertes = config.getOrDefault("fuerte", List.of()).stream().filter(t -> Pattern.compile(".*\\b" + Pattern.quote(t) + "\\b.*").matcher(texto).matches()).count();
        long medias = config.getOrDefault("media", List.of()).stream().filter(t -> Pattern.compile(".*\\b" + Pattern.quote(t) + "\\b.*").matcher(texto).matches()).count();
        long debiles = config.getOrDefault("debil", List.of()).stream().filter(t -> Pattern.compile(".*\\b" + Pattern.quote(t) + "\\b.*").matcher(texto).matches()).count();

        penalizacionExclusion -= fuertes * 5.0;
        penalizacionExclusion -= medias * 2.0;
        penalizacionExclusion -= debiles * 1.0;

        coincidencias.addAndGet((int) (fuertes + medias + debiles));

        return penalizacionExclusion;
    }

    private double calcularScorePatrones(Map<String, Map<String, String>> config, String texto, AtomicInteger coincidencias) {
        double score = 0.0;
        if (config == null) return 0.0;

        for (Map<String, String> mapaDePatrones : config.values()) {
            for (String regex : mapaDePatrones.values()) {
                Pattern pattern = Pattern.compile(".*" + regex + ".*");
                if (pattern.matcher(texto).matches()) {
                    score += 1.0;
                    coincidencias.incrementAndGet();
                }
            }
        }
        return score;
    }

    private double calcularBonificaciones(Object bonificacionesConfig, String texto, AtomicInteger coincidencias) {
        double bonusScore = 0.0;
        if (bonificacionesConfig == null) return 0.0;

        List<BonusRuleDTO> reglas = objectMapper.convertValue(bonificacionesConfig, new TypeReference<List<BonusRuleDTO>>() {});
        for (BonusRuleDTO regla : reglas) {
            Pattern pattern = Pattern.compile(regla.condicion());
            if (pattern.matcher(texto).matches()){
                bonusScore += regla.puntos();
                coincidencias.incrementAndGet();
            }
        }
        return bonusScore;
    }
}
