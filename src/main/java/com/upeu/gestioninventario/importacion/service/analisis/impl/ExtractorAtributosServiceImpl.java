package com.upeu.gestioninventario.importacion.service.analisis.impl;

import com.upeu.gestioninventario.shared.services.rules.CommonAttributeRules;
import com.upeu.gestioninventario.importacion.service.analisis.IExtractorAtributos;
import com.upeu.gestioninventario.ml.dto.seed.AtributoExtraccionSeedDTO;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import com.upeu.gestioninventario.importacion.dto.internal.ReconocimientoConfigDTO;
import com.upeu.gestioninventario.importacion.dto.internal.EstrategiaExtraccionDTO;
import com.upeu.gestioninventario.importacion.dto.internal.ExtraccionConfigDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractorAtributosServiceImpl implements IExtractorAtributos {

    private final ObjectMapper objectMapper;
    private final CommonAttributeRules commonAttributeRules;

    @Override
    public Map<String, String> extraerAtributos(String textoConcatenado, Map<String, String> datosPorColumna, PlantillaSeedDTO plantilla) {
        log.info("Iniciando extracción de atributos para plantilla '{}'", plantilla.metadatos().nombre());
        Map<String, String> atributosExtraidos = new HashMap<>();
        ReconocimientoConfigDTO reconocimientoConfig = objectMapper.convertValue(plantilla.reconocimiento(), ReconocimientoConfigDTO.class);

        List<AtributoExtraccionSeedDTO> reglasComunes = commonAttributeRules.getCommonRules();
        List<AtributoExtraccionSeedDTO> reglasDePlantilla = plantilla.atributosExtraccion() != null ? plantilla.atributosExtraccion() : List.of();

        List<AtributoExtraccionSeedDTO> reglas = new ArrayList<>();
        reglas.addAll(reglasComunes);
        reglas.addAll(reglasDePlantilla);

        for (AtributoExtraccionSeedDTO regla : reglas) {
            String nombreAtributo = regla.nombreCampo();
            ExtraccionConfigDTO config = objectMapper.convertValue(regla.extraccion(), ExtraccionConfigDTO.class);
            if (config == null || config.estrategias() == null || config.estrategias().isEmpty()) {
                log.trace("No se encontraron estrategias de extracción para el atributo '{}'. Se omitirá.", nombreAtributo);
                continue;
            }
            for (EstrategiaExtraccionDTO estrategia : config.estrategias()) {
                Optional<String> valorEncontrado = ejecutarEstrategia(estrategia, textoConcatenado, datosPorColumna, reconocimientoConfig);
                if (valorEncontrado.isPresent() && !valorEncontrado.get().isBlank()) {
                    atributosExtraidos.merge(nombreAtributo, valorEncontrado.get(), (existente, nuevo) -> estrategia.multiple() ? existente + ", " + nuevo : nuevo);
                    log.debug("Atributo '{}' encontrado con estrategia '{}': {}", nombreAtributo, estrategia.tipo(), valorEncontrado.get());
                    if (!estrategia.multiple()) break;
                }
            }
        }
        log.info("Extracción completada. Se encontraron {} atributos.", atributosExtraidos.size());
        return atributosExtraidos;
    }

    private Optional<String> ejecutarEstrategia(EstrategiaExtraccionDTO estrategia, String textoConcatenado, Map<String, String> datosPorColumna, ReconocimientoConfigDTO reconocimientoConfig) {
        return switch (estrategia.tipo()) {
            case "COLUMNA_MAPEADA" -> ejecutarEstrategiaColumna(estrategia, datosPorColumna);
            case "ETIQUETA", "PATRON" -> ejecutarEstrategiaRegex(estrategia, textoConcatenado);
            case "MARCA_CONOCIDA" -> ejecutarEstrategiaMarcaConocida(estrategia, textoConcatenado, reconocimientoConfig);
            default -> Optional.empty();
        };
    }

    private Optional<String> ejecutarEstrategiaColumna(EstrategiaExtraccionDTO estrategia, Map<String, String> datosPorColumna) {
        if (estrategia.nombresColumna() == null) return Optional.empty();

        for (String aliasColumna : estrategia.nombresColumna()) {
            String valor = datosPorColumna.get(aliasColumna.toLowerCase());
            if (valor != null && !valor.isBlank()) {
                return Optional.of(valor.trim());
            }
        }
        return Optional.empty();
    }

    private Optional<String> ejecutarEstrategiaRegex(EstrategiaExtraccionDTO estrategia, String texto) {
        if (estrategia.patron() == null || estrategia.patron().isBlank()) return Optional.empty();

        try {
            Pattern pattern = Pattern.compile(estrategia.patron(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(texto);

            if (estrategia.multiple()) {
                List<String> valores = new ArrayList<>();
                while (matcher.find()) {
                    String valor = matcher.group(estrategia.grupo());
                    if (valor != null && !valor.isBlank()) {
                        valores.add(valor.trim());
                    }
                }
                return valores.isEmpty() ? Optional.empty() : Optional.of(String.join(", ", valores));
            } else {
                if (matcher.find()) {
                    String valor = matcher.group(estrategia.grupo());
                    if (valor != null && !valor.isBlank()) {
                        return Optional.of(valor.trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error al ejecutar regex '{}': {}", estrategia.patron(), e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<String> ejecutarEstrategiaMarcaConocida(EstrategiaExtraccionDTO estrategia, String texto, ReconocimientoConfigDTO reconocimientoConfig) {
        if (reconocimientoConfig == null || reconocimientoConfig.marcas() == null || estrategia.fuente() == null) {
            return Optional.empty();
        }

        List<String> marcasABuscar;
        if ("marcas.*".equals(estrategia.fuente())) {
            log.debug("Estrategia 'MARCA_CONOCIDA' usando fuente dinámica: {}", estrategia.fuente());
            marcasABuscar = reconocimientoConfig.marcas().values().stream()
                    .flatMap(List::stream)
                    .toList();
        } else {
            return Optional.empty();
        }

        for (String marca : marcasABuscar) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(marca) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(texto);
            if (matcher.find()) {
                return Optional.of(matcher.group());
            }
        }
        return Optional.empty();
    }
}
