package com.upeu.gestioninventario.inventario.service.impl;

import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.categorias.repository.TipoAtributoRepository;
import com.upeu.gestioninventario.inventario.dto.bien.BienDTO;
import com.upeu.gestioninventario.inventario.mapper.BienMapper;
import com.upeu.gestioninventario.inventario.model.Bien;
import com.upeu.gestioninventario.inventario.model.BienAtributoValor;
import com.upeu.gestioninventario.inventario.repository.BienRepository;
import com.upeu.gestioninventario.inventario.service.IBienPersistenciaService;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.shared.services.rules.CommonAttributeRules;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
@Slf4j
public class BienPersistenciaServiceImpl implements IBienPersistenciaService {

    private final BienRepository bienRepository;
    private final BienMapper bienMapper;
    private final TipoAtributoRepository tipoAtributoRepository;
    private final CommonAttributeRules commonAttributeRules;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BienDTO guardarBien(Map<String, String> campos, Categoria categoria, Persona responsable, Ubicacion ubicacion, Departamento departamento) {
        log.info("Guardando bien - Categoría: {}", categoria.getNombreCategoria());

        validarIdentificadores(campos);

        Bien bien = construirBien(campos, categoria, responsable, ubicacion, departamento);
        agregarAtributosDinamicos(bien, campos);

        Bien guardado = bienRepository.save(bien);
        log.info("Bien guardado ID: {}", guardado.getId());

        return bienMapper.toDto(guardado);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BienDTO actualizarBienExistente(Long bienId, Map<String, String> campos, Categoria categoria, Ubicacion ubicacion) {
        log.info("Actualizando bien ID: {} desde importación", bienId);

        Bien bien = bienRepository.findByIdWithDetails(bienId)
                .orElseThrow(() -> new IllegalArgumentException("Bien no encontrado: " + bienId));

        if (categoria != null) bien.setCategoria(categoria);
        if (ubicacion != null) bien.setUbicacionActual(ubicacion);

        actualizarCamposBase(bien, campos);
        actualizarAtributosDinamicos(bien, campos);

        return bienMapper.toDto(bienRepository.save(bien));
    }

    // CONSTRUCCIÓN
    private Bien construirBien(Map<String, String> campos, Categoria cat, Persona resp, Ubicacion ubi, Departamento dept) {
        return Bien.builder()
                .nombreBien(campos.getOrDefault("NOMBRE_BIEN", "Sin Nombre"))
                .caf(campos.get("CAF"))
                .numeroSerie(campos.get("NUMERO_SERIE"))
                .observaciones(campos.get("OBSERVACIONES"))
                .estadoFisico(campos.getOrDefault("ESTADO_FISICO", "Bueno"))
                .estadoOperacional(campos.getOrDefault("ESTADO_OPERACIONAL", "Operativo"))
                .categoria(cat)
                .responsableActual(resp)
                .departamento(dept)
                .ubicacionActual(ubi)
                .atributos(new HashSet<>())
                .build();
    }

    // VALIDACIÓN
    private static final java.util.Set<String> VALORES_GENERICOS_CAF = java.util.Set.of(
            // Estados de propiedad
            "arrendado", "prestado", "donado", "comodato", "alquilado",
            // Valores vacíos o desconocidos
            "-", "--", "n/a", "na", "no aplica", "sin caf",
            "no existe", "no encontrado", "pendiente", "desconocido",
            "por asignar", "sin asignar", "temporal"
    );
    
    private static final String CAF_POR_DEFECTO = "NO ENCONTRADO";

    private void validarIdentificadores(Map<String, String> campos) {
        String caf = normalizarCAF(campos.get("CAF"));
        String serie = campos.get("NUMERO_SERIE");
        
        campos.put("CAF", caf);

        boolean tieneCafValido = !esInvalido(caf);
        boolean tieneSerieValida = !esInvalido(serie);
        
        if (!tieneCafValido && !tieneSerieValida) {
            throw new IllegalArgumentException("Se requiere CAF o Número de Serie");
        }
        
        if (tieneCafValido && !esValorGenericoCAF(caf)) {
            bienRepository.findByCaf(caf).ifPresent(b -> {
                throw new IllegalArgumentException("CAF '" + caf + "' ya existe");
            });
        }
        
        if (tieneSerieValida) {
            bienRepository.findByNumeroSerie(serie).ifPresent(b -> {
                throw new IllegalArgumentException("Serie '" + serie + "' ya existe");
            });
        }
    }

    private String normalizarCAF(String caf) {
        if (caf == null || caf.isBlank()) {
            return CAF_POR_DEFECTO;
        }
        String cafLimpio = caf.trim();
        // Si es valor genérico, normalizarlo a mayúsculas
        if (esValorGenericoCAF(cafLimpio)) {
            return cafLimpio.toUpperCase();
        }
        return cafLimpio;
    }

    private boolean esValorGenericoCAF(String caf) {
        if (caf == null || caf.isBlank()) return true;
        return VALORES_GENERICOS_CAF.contains(caf.toLowerCase().trim());
    }

    private boolean esInvalido(String v) {
        return v == null || v.isBlank() || "DESCONOCIDO".equalsIgnoreCase(v);
    }

    // ATRIBUTOS
    private void agregarAtributosDinamicos(Bien bien, Map<String, String> campos) {
        campos.entrySet().stream()
                .filter(e -> !commonAttributeRules.esCampoBase(e.getKey()))
                .forEach(e -> tipoAtributoRepository.findByNombreAtributo(e.getKey())
                        .ifPresent(tipo -> bien.getAtributos().add(
                                BienAtributoValor.builder()
                                        .bien(bien)
                                        .tipoAtributo(tipo)
                                        .valor(e.getValue())
                                        .build()
                        ))
                );
    }

    private void actualizarCamposBase(Bien bien, Map<String, String> campos) {
        bien.setNombreBien(campos.getOrDefault("NOMBRE_BIEN", bien.getNombreBien()));
        bien.setNumeroSerie(campos.getOrDefault("NUMERO_SERIE", bien.getNumeroSerie()));
        bien.setObservaciones(campos.getOrDefault("OBSERVACIONES", bien.getObservaciones()));
        bien.setEstadoFisico(campos.getOrDefault("ESTADO_FISICO", bien.getEstadoFisico()));
        bien.setEstadoOperacional(campos.getOrDefault("ESTADO_OPERACIONAL", bien.getEstadoOperacional()));
    }

    private void actualizarAtributosDinamicos(Bien bien, Map<String, String> campos) {
        Map<Long, BienAtributoValor> existentes = bien.getAtributos().stream()
                .collect(Collectors.toMap(a -> a.getTipoAtributo().getId(), a -> a));

        campos.entrySet().stream()
                .filter(e -> !commonAttributeRules.esCampoBase(e.getKey()))
                .forEach(e -> tipoAtributoRepository.findByNombreAtributo(e.getKey())
                        .ifPresent(tipo -> {
                            BienAtributoValor attr = existentes.get(tipo.getId());
                            if (attr != null) {
                                attr.setValor(e.getValue());
                            } else {
                                bien.getAtributos().add(BienAtributoValor.builder()
                                        .bien(bien).tipoAtributo(tipo).valor(e.getValue()).build());
                            }
                        })
                );
    }
}