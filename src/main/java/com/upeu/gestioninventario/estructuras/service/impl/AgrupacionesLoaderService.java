package com.upeu.gestioninventario.estructuras.service.impl;

import com.upeu.gestioninventario.configuracion.service.ISeedStateManager;
import com.upeu.gestioninventario.estructuras.dto.config.AgrupacionDTO;
import com.upeu.gestioninventario.estructuras.dto.config.AgrupacionesConfigDTO;
import com.upeu.gestioninventario.shared.utils.hash.IHashCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgrupacionesLoaderService {

    private static final String ARCHIVO_AGRUPACIONES = "agrupaciones-componentes.json";
    private static final String RUTA_AGRUPACIONES = "seed/estructuras/componentes_agrupaciones/agrupaciones-componentes.json";
    private static final String TIPO_SEED = "AGRUPACIONES";

    private final ObjectMapper objectMapper;
    private final ISeedStateManager seedStateManager;
    private final IHashCalculator hashCalculator;

    @Getter
    private AgrupacionesConfigDTO configuracion;

    @Getter
    private boolean huboCambios = false;

    private final Map<String, AgrupacionDTO> agrupacionesPorId = new ConcurrentHashMap<>();
    private final Map<String, AgrupacionDTO> agrupacionesPorTipo = new ConcurrentHashMap<>();

    @PostConstruct
    public void cargarAgrupaciones() {
        log.info("Iniciando carga de agrupaciones de componentes...");

        try {
            ClassPathResource resource = new ClassPathResource(RUTA_AGRUPACIONES);
            
            String hashActual = hashCalculator.calcularHash(resource);
            boolean necesitaRecarga = seedStateManager.necesitaRecarga(ARCHIVO_AGRUPACIONES, hashActual);

            if (!necesitaRecarga && configuracion != null) {
                log.debug("Agrupaciones sin cambios, usando cache existente");
                return;
            }

            huboCambios = necesitaRecarga;

            try (InputStream inputStream = resource.getInputStream()) {
                configuracion = objectMapper.readValue(inputStream, AgrupacionesConfigDTO.class);
            }

            if (configuracion.getAgrupaciones() != null) {
                agrupacionesPorId.clear();
                agrupacionesPorTipo.clear();
                
                for (AgrupacionDTO agrupacion : configuracion.getAgrupaciones()) {
                    agrupacionesPorId.put(agrupacion.getId(), agrupacion);
                    agrupacionesPorTipo.put(agrupacion.getNombreComponente().toUpperCase(), agrupacion);
                }
            }

            seedStateManager.registrarCarga(ARCHIVO_AGRUPACIONES, RUTA_AGRUPACIONES, TIPO_SEED, hashActual);
            log.info("Agrupaciones cargadas: {} definidas (cambios: {})", agrupacionesPorId.size(), huboCambios);

        } catch (Exception e) {
            log.error("Error al cargar agrupaciones: {}", e.getMessage());
            configuracion = new AgrupacionesConfigDTO();
        }
    }
}