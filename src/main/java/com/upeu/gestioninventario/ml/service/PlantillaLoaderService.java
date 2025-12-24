package com.upeu.gestioninventario.ml.service;

import com.upeu.gestioninventario.configuracion.service.ISeedStateManager;
import com.upeu.gestioninventario.shared.config.properties.PlantillasProperties;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import com.upeu.gestioninventario.shared.utils.hash.IHashCalculator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.upeu.gestioninventario.shared.utils.StringUtils.toUpperSnakeCase;


@Service
@Slf4j
@RequiredArgsConstructor
public class PlantillaLoaderService {

    private static final String TIPO_SEED = "PLANTILLAS";

    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourceLoader;
    private final PlantillasProperties plantillasProperties;
    private final ISeedStateManager seedStateManager;
    private final IHashCalculator hashCalculator;

    @Getter
    private final Map<String, PlantillaSeedDTO> plantillasCache = new ConcurrentHashMap<>();

    @Getter
    private final Map<String, String> mapeoIdPlantillaCategoriaDB = new ConcurrentHashMap<>();

    @Getter
    private final Map<String, String> mapeoCategoriaDBIdPlantilla = new ConcurrentHashMap<>();

    @Getter
    private final Set<String> plantillasCambiadas = new HashSet<>();

    private JsonNode configuracion;
    private boolean usarConvenciones = true;

    @PostConstruct
    public void inicializadorSistema() {
        long inicio = System.currentTimeMillis();
        log.info("Inicializando plantilla loader");
        try {
            cargarConfiguracion();
            Map<String, Integer> estadisticas = autoDescubrimientoYCargar();
            long tiempoCarga =  System.currentTimeMillis() - inicio;
            mostrarEstadisticas(estadisticas, tiempoCarga);
        } catch (Exception e) {
            log.error("Error crítico al inicializar sistema de plantillas", e);
            log.warn("Sistema continuará con  plantillas cargadas");
        }
    }

    private void cargarConfiguracion() throws IOException {
        String rutaCompleta = "classpath:" + plantillasProperties.getDirectorio() + "/" + plantillasProperties.getConfig();
        log.info("Cargando archivo configuracion para plantilla loader desde: {}", rutaCompleta);

        Resource resource = resourceLoader.getResource(rutaCompleta);
        if (!resource.exists()) {
            log.warn("El archivo de configuración principal '{}' no fue encontrado. Se usarán valores por defecto.", rutaCompleta);
            this.configuracion = objectMapper.createObjectNode();
            this.usarConvenciones = true;
            return;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            this.configuracion = objectMapper.readTree(inputStream);
            log.info("Archivo de configuración principal cargado y parseado correctamente.");
            this.usarConvenciones = configuracion.path("configuracion").path("usarConvencionNombres").asBoolean(true);
            log.info("Configuración 'usarConvenciones' establecida en: {}", this.usarConvenciones);
        } catch (IOException e) {
            log.error("Error al leer el archivo de configuración principal: {}", rutaCompleta, e);
            throw e;
        }
    }

    private Map<String, Integer> autoDescubrimientoYCargar() throws IOException {
        Map<String, Integer> estadistica = new HashMap<>();
        JsonNode gruposNode = configuracion.path("grupos");
        if (gruposNode.isMissingNode() || !gruposNode.isObject()) {
            log.warn("No se encontró la sección 'grupos' en el archivo de configuración. No se cargarán plantillas.");
            return estadistica;
        }
        for (Map.Entry<String, JsonNode> grupoEntry : gruposNode.properties()) {
            String nombreGrupo = grupoEntry.getKey();
            JsonNode grupoConfig = grupoEntry.getValue();
            if (!grupoConfig.path("autoDescubrir").asBoolean(false)) {
                log.info("Grupo '{}' tiene el auto-descubrimiento desactivado. Omitiendo.", nombreGrupo);
                continue;
            }

            if (grupoConfig.has("carpeta")) {
                String carpeta = grupoConfig.path("carpeta").asText();
                if (!carpeta.isEmpty()) {
                    log.info("Procesando grupo '{}' en la carpeta '{}'", nombreGrupo, carpeta);
                    int cargadas = cargarPlantillasDeCarpeta(carpeta);
                    estadistica.merge(nombreGrupo, cargadas, Integer::sum);
                }
            }
            if (grupoConfig.has("carpetas") && grupoConfig.get("carpetas").isArray()) {
                log.info("Procesando grupo '{}' con múltiples carpetas...", nombreGrupo);
                for (JsonNode carpetaNode : grupoConfig.get("carpetas")) {
                    String nombreCarpeta = carpetaNode.asText();
                    log.info("  -> Procesando sub-carpeta '{}'", nombreCarpeta);
                    int cargadas = cargarPlantillasDeCarpeta(nombreCarpeta);
                    estadistica.merge(nombreGrupo, cargadas, Integer::sum);
                }
            }
        }
        return estadistica;
    }

    private int cargarPlantillasDeCarpeta(String carpeta) throws IOException {
        String rutaBusqueda = "classpath*:" + plantillasProperties.getDirectorio() + "/" + carpeta + "/*.json";
        Resource[] resources = resourceLoader.getResources(rutaBusqueda);
        int contador = 0;

        for (Resource resource : resources) {
            String nombreArchivo = resource.getFilename();
            if (nombreArchivo == null) continue;

            try {
                String hashActual = hashCalculator.calcularHash(resource);
                boolean necesitaRecarga = seedStateManager.necesitaRecarga(nombreArchivo, hashActual);

                try (InputStream inputStream = resource.getInputStream()) {
                    PlantillaSeedDTO plantilla = objectMapper.readValue(inputStream, PlantillaSeedDTO.class);
                    String idPlantilla = plantilla.metadatos().idPlantilla();

                    if (idPlantilla == null || idPlantilla.isBlank()) {
                        log.warn("Plantilla en archivo '{}' no tiene 'idPlantilla'. Omitiendo.", nombreArchivo);
                        continue;
                    }

                    plantillasCache.put(idPlantilla, plantilla);
                    String nombreCategoriaBD = generarNombreCategoriaBD(nombreArchivo);
                    mapeoIdPlantillaCategoriaDB.put(idPlantilla, nombreCategoriaBD);
                    mapeoCategoriaDBIdPlantilla.put(nombreCategoriaBD, idPlantilla);

                    if (necesitaRecarga) {
                        plantillasCambiadas.add(idPlantilla);
                        String rutaArchivo = plantillasProperties.getDirectorio() + "/" + carpeta + "/" + nombreArchivo;
                        seedStateManager.registrarCarga(nombreArchivo, rutaArchivo, TIPO_SEED, hashActual);
                        log.info("Plantilla cargada: '{}' (ID: {}, cambio detectado)", 
                                plantilla.metadatos().nombre(), idPlantilla);
                    } else {
                        log.debug("Plantilla '{}' (ID: {}) sin cambios", 
                                plantilla.metadatos().nombre(), idPlantilla);
                    }

                    contador++;
                }
            } catch (Exception e) {
                log.error("Error al cargar o procesar la plantilla desde el archivo: {}", nombreArchivo, e);
            }
        }
        return contador;
    }

    private String generarNombreCategoriaBD(String nombreArchivo) {
        if (!usarConvenciones) {
            return toUpperSnakeCase(nombreArchivo.replace(".json", ""));
        }

        String formato = configuracion.path("convenciones").path("formatoCategoriaBD").asText("{CATEGORIA_UPPER_SNAKE}");
        String baseNombre = nombreArchivo.replace(".json", "");

        return formato.replace("{CATEGORIA_UPPER_SNAKE}", toUpperSnakeCase(baseNombre));
    }

    private void mostrarEstadisticas(Map<String, Integer> estadisticas, long tiempoCarga) {
        log.info("Tiempo total de carga: {} ms", tiempoCarga);
        log.info("Total de plantillas cargadas en caché: {}", plantillasCache.size());
        estadisticas.forEach((grupo, count) -> log.info(" - Grupo '{}': {} plantillas", grupo, count));
    }

}
