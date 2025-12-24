package com.upeu.gestioninventario.importacion.service.analisis.impl;

import com.upeu.gestioninventario.configuracion.service.ISeedStateManager;
import com.upeu.gestioninventario.importacion.dto.formato.clavevalor.FormatoClaveValorDTO;
import com.upeu.gestioninventario.importacion.dto.formato.common.MetadataReconocimientoDTO;
import com.upeu.gestioninventario.importacion.dto.formato.matriz.FormatoMatrizDTO;
import com.upeu.gestioninventario.importacion.dto.formato.tabular.FormatoTabularDTO;
import com.upeu.gestioninventario.importacion.service.analisis.IFormatoConfigLoader;
import com.upeu.gestioninventario.shared.utils.hash.IHashCalculator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
@Slf4j
public class FormatoConfigServiceImpl implements IFormatoConfigLoader {

    private static final String TIPO_SEED = "FORMATOS";

    private final ObjectMapper objectMapper;
    private final ISeedStateManager seedStateManager;
    private final IHashCalculator hashCalculator;

    private FormatoTabularDTO formatoTabular;
    private FormatoClaveValorDTO formatoClaveValor;
    private FormatoMatrizDTO formatoMatriz;
    private Map<String, MetadataReconocimientoDTO> metadataReconocimiento;

    @Getter
    private boolean huboCambios = false;

    private static final String RUTA_TABULAR = "seed/formatos/tabular.json";
    private static final String RUTA_CLAVE_VALOR = "seed/formatos/clave_valor.json";
    private static final String RUTA_MATRIZ = "seed/formatos/matriz.json";
    private static final String RUTA_METADATA = "seed/formatos/metadata.json";

    public FormatoConfigServiceImpl(ObjectMapper objectMapper, 
                                    ISeedStateManager seedStateManager,
                                    IHashCalculator hashCalculator) {
        this.objectMapper = objectMapper;
        this.seedStateManager = seedStateManager;
        this.hashCalculator = hashCalculator;
    }

    @PostConstruct
    public void inicializarConfiguracionesFormatos() {
        log.info("Iniciando carga de configuraciones de formatos");
        this.formatoTabular = cargarFormato(RUTA_TABULAR, FormatoTabularDTO.class, "TABULAR");
        this.formatoClaveValor = cargarFormato(RUTA_CLAVE_VALOR, FormatoClaveValorDTO.class, "CLAVE_VALOR");
        this.formatoMatriz = cargarFormato(RUTA_MATRIZ, FormatoMatrizDTO.class, "MATRIZ");
        cargarMetadata();
        log.info("Configuraciones de formatos cargadas (cambios: {})", huboCambios);
    }

    private <T> T cargarFormato(String ruta, Class<T> classDTO, String nombreFormato) {
        try {
            ClassPathResource resource = new ClassPathResource(ruta);
            String nombreArchivo = resource.getFilename();
            
            String hashActual = hashCalculator.calcularHash(resource);
            boolean necesitaRecarga = seedStateManager.necesitaRecarga(nombreArchivo, hashActual);

            if (necesitaRecarga) {
                huboCambios = true;
                log.info("Cargando configuracion {}...", nombreFormato);
            } else {
                log.debug("Formato {} sin cambios", nombreFormato);
            }

            try (InputStream inputStream = resource.getInputStream()) {
                T configuracion = objectMapper.readValue(inputStream, classDTO);
                if (configuracion == null) {
                    throw new IllegalStateException("La configuracion " + nombreFormato + " es null despues de cargar");
                }

                if (necesitaRecarga) {
                    seedStateManager.registrarCarga(nombreArchivo, ruta, TIPO_SEED, hashActual);
                    log.info("Formato {} cargado correctamente", nombreFormato);
                }

                return configuracion;
            }

        } catch (IOException e) {
            log.error("Error al cargar formato {}: {}", nombreFormato, e.getMessage());
            throw new RuntimeException("No se pudo cargar la configuracion " + nombreFormato, e);
        }
    }

    private void cargarMetadata() {
        try {
            ClassPathResource resource = new ClassPathResource(RUTA_METADATA);
            String nombreArchivo = resource.getFilename();

            String hashActual = hashCalculator.calcularHash(resource);
            boolean necesitaRecarga = seedStateManager.necesitaRecarga(nombreArchivo, hashActual);

            if (necesitaRecarga) {
                huboCambios = true;
                log.info("Cargando METADATA de reconocimiento...");
            } else {
                log.debug("Metadata sin cambios");
            }

            try (InputStream inputStream = resource.getInputStream()) {
                TypeReference<Map<String, MetadataReconocimientoDTO>> typeRef = new TypeReference<>() {};
                this.metadataReconocimiento = objectMapper.readValue(inputStream, typeRef);

                if (this.metadataReconocimiento == null || this.metadataReconocimiento.isEmpty()) {
                    throw new IllegalStateException("La metadata esta vacia o es null");
                }

                if (necesitaRecarga) {
                    seedStateManager.registrarCarga(nombreArchivo, RUTA_METADATA, TIPO_SEED, hashActual);
                    log.info("Metadata cargada correctamente: {} entries", this.metadataReconocimiento.size());
                }
            }

        } catch (IOException e) {
            log.error("Error al cargar metadata: {}", e.getMessage());
            throw new RuntimeException("No se pudo cargar la metadata", e);
        }
    }

    @Override
    public FormatoTabularDTO obtenerFormatoTabular() {
        return formatoTabular;
    }

    @Override
    public FormatoClaveValorDTO obtenerFormatoClaveValor() {
        return formatoClaveValor;
    }

    @Override
    public FormatoMatrizDTO obtenerFormatoMatriz() {
        return formatoMatriz;
    }

}
