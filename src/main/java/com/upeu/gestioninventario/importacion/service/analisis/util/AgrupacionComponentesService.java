package com.upeu.gestioninventario.importacion.service.analisis.util;

import com.upeu.gestioninventario.importacion.dto.agrupacion.AgrupacionDTO;
import com.upeu.gestioninventario.importacion.dto.agrupacion.ConfiguracionAgrupacionesDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
public class AgrupacionComponentesService {
    
    private static final String RUTA_AGRUPACIONES = "seed/estructuras/componentes_agrupaciones/agrupaciones-componentes.json";
    
    @Getter
    private ConfiguracionAgrupacionesDTO configuracion;
    private final ObjectMapper objectMapper;
    
    public AgrupacionComponentesService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void cargarConfiguracion() {
        try {
            ClassPathResource resource = new ClassPathResource(RUTA_AGRUPACIONES);
            try (InputStream is = resource.getInputStream()) {
                configuracion = objectMapper.readValue(is, ConfiguracionAgrupacionesDTO.class);
                log.info("Configuración de agrupaciones cargada: {} agrupaciones definidas", 
                        configuracion.getAgrupaciones().size());
            }
        } catch (Exception e) {
            log.error("Error al cargar configuración de agrupaciones: {}", e.getMessage());
            configuracion = crearConfiguracionPorDefecto();
        }
    }
    
    public Optional<AgrupacionDTO> detectarAgrupacionPorBienes(List<String> nombresBienes) {
        if (nombresBienes == null || nombresBienes.isEmpty() || configuracion == null) {
            return Optional.empty();
        }
        
        for (AgrupacionDTO agrupacion : configuracion.getAgrupaciones()) {
            int componentesCoincidentes = contarComponentesCoincidentes(agrupacion, nombresBienes);
            int minimoRequerido = agrupacion.getConfiguracion() != null 
                    ? agrupacion.getConfiguracion().getMinimoComponentes() 
                    : 2;
            
            if (componentesCoincidentes >= minimoRequerido) {
                log.debug("Agrupación '{}' detectada con {} componentes coincidentes", 
                        agrupacion.getNombreComponente(), componentesCoincidentes);
                return Optional.of(agrupacion);
            }
        }
        
        return Optional.empty();
    }

    public boolean esPeriferico(String nombreBien) {
        if (nombreBien == null || configuracion == null) {
            return false;
        }
        
        return configuracion.getAgrupaciones().stream()
                .filter(agr -> "PERIFERICO".equals(agr.getTipo()))
                .anyMatch(agr -> agr.contieneComponente(nombreBien));
    }
    
    public String getTipoComponente(String nombreBien) {
        if (nombreBien == null || configuracion == null) {
            return "PERIFERICO";
        }
        
        Optional<AgrupacionDTO> agrupacion = configuracion.getAgrupaciones().stream()
                .filter(agr -> agr.contieneComponente(nombreBien))
                .findFirst();
        
        return agrupacion.map(AgrupacionDTO::getTipo).orElse("PERIFERICO");
    }

    
    public Optional<AgrupacionDTO> getAgrupacionPerifericos() {
        if (configuracion == null) {
            return Optional.empty();
        }
        
        return configuracion.getAgrupaciones().stream()
                .filter(agr -> "PERIFERICO".equals(agr.getTipo()))
                .findFirst();
    }

    private int contarComponentesCoincidentes(AgrupacionDTO agrupacion, List<String> nombresBienes) {
        if (agrupacion.getComponentesQueAgrupa() == null) {
            return 0;
        }
        
        int count = 0;
        for (String nombreBien : nombresBienes) {
            if (agrupacion.contieneComponente(nombreBien)) {
                count++;
            }
        }
        return count;
    }
    
    private ConfiguracionAgrupacionesDTO crearConfiguracionPorDefecto() {
        ConfiguracionAgrupacionesDTO config = new ConfiguracionAgrupacionesDTO();
        config.setVersion("1.0");
        config.setAgrupaciones(new ArrayList<>());
        log.warn("Usando configuración de agrupaciones por defecto (vacía)");
        return config;
    }
}

