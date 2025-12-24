package com.upeu.gestioninventario.categorias.service.impl;

import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.categorias.repository.CategoriaRepository;
import com.upeu.gestioninventario.categorias.service.IFormularioDinamicoService;
import com.upeu.gestioninventario.inventario.dto.AtributoFormularioDTO;
import com.upeu.gestioninventario.ml.mapper.PlantillaAtributoMapper;
import com.upeu.gestioninventario.ml.model.PlantillaAtributo;
import com.upeu.gestioninventario.ml.repository.PlantillaAtributoRepository;
import com.upeu.gestioninventario.shared.services.rules.CommonAttributeRules;
import com.upeu.gestioninventario.ml.dto.seed.AtributoExtraccionSeedDTO;
import com.upeu.gestioninventario.shared.utils.naming.EstandarizadorNombresService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormularioDinamicoServiceImpl implements IFormularioDinamicoService {

    private static final int ORDEN_BASE_DINAMICOS = 20;

    private final CategoriaRepository categoriaRepository;
    private final PlantillaAtributoRepository plantillaAtributoRepository;
    private final PlantillaAtributoMapper plantillaAtributoMapper;
    private final EstandarizadorNombresService estandarizadorNombresService;
    private final CommonAttributeRules commonAttributeRules;

    @Override
    @Transactional(readOnly = true)
    public List<AtributoFormularioDTO> obtenerAtributosParaCategoria(Long categoriaId, String buscarNombrePlantilla) {
        log.info("Generando formulario. CategoriaID: {}, Plantilla: '{}'", categoriaId, buscarNombrePlantilla);

        NombreResuelto nombre = resolverNombre(categoriaId, buscarNombrePlantilla);

        List<AtributoFormularioDTO> camposBase = generarCamposBase();
        Set<String> nombresBase = extraerNombres(camposBase);

        List<AtributoFormularioDTO> camposDinamicos = obtenerCamposDinamicos(nombre.plantilla(), nombresBase);

        List<AtributoFormularioDTO> resultado = combinarYOrdenar(camposBase, camposDinamicos);

        log.info("Generados {} atributos para '{}' ({} base + {} dinámicos)",
                resultado.size(), nombre.original(), camposBase.size(), camposDinamicos.size());

        return resultado;
    }

    private NombreResuelto resolverNombre(Long categoriaId, String nombrePlantilla) {
        if (categoriaId != null) {
            Categoria cat = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + categoriaId));
            return new NombreResuelto(
                    cat.getNombreCategoria(),
                    estandarizadorNombresService.obtenerNombreCategoriaSingular(cat.getNombreCategoria())
            );
        }

        if (nombrePlantilla != null && !nombrePlantilla.isBlank()) {
            return new NombreResuelto(
                    nombrePlantilla,
                    estandarizadorNombresService.obtenerNombreCategoriaSingular(nombrePlantilla)
            );
        }

        throw new IllegalArgumentException("Debe proporcionar categoriaId o buscarNombrePlantilla");
    }

    private List<AtributoFormularioDTO> generarCamposBase() {
        return commonAttributeRules.getCommonRules().stream()
                .map(this::convertirRegla)
                .collect(Collectors.toList());
    }

    private AtributoFormularioDTO convertirRegla(AtributoExtraccionSeedDTO regla) {
        return new AtributoFormularioDTO(
                null,
                regla.nombreCampo(),
                regla.etiqueta(),
                regla.tipoDato(),
                Boolean.TRUE.equals(regla.requerido()),
                regla.valorPorDefecto(),
                regla.ordenUI() != null ? regla.ordenUI() : 0,
                regla.placeholder(),
                extraerOpciones(regla)
        );
    }

    private List<String> extraerOpciones(AtributoExtraccionSeedDTO regla) {
        if (!"LISTA".equals(regla.tipoDato()) || regla.extraccion() == null) {
            return null;
        }

        Map<String, Object> ext = regla.extraccion();

        List<String> opciones = toStringList(ext.get("opciones"));
        if (opciones != null) return opciones;

        return extraerDeEstrategias(ext);
    }

    @SuppressWarnings("unchecked")
    private List<String> extraerDeEstrategias(Map<String, Object> ext) {
        Object obj = ext.get("estrategias");
        if (!(obj instanceof List<?>)) return null;

        for (Map<String, Object> estrategia : (List<Map<String, Object>>) obj) {
            if ("PALABRAS".equals(estrategia.get("tipo"))) {
                List<String> lista = toStringList(estrategia.get("lista"));
                if (lista != null) return lista;
            }
        }
        return null;
    }

    private List<String> toStringList(Object obj) {
        if (!(obj instanceof List<?> lista)) return null;

        List<String> result = lista.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toList());

        return result.isEmpty() ? null : result;
    }


    private List<AtributoFormularioDTO> obtenerCamposDinamicos(String nombrePlantilla, Set<String> nombresBase) {
        List<PlantillaAtributo> atributos = plantillaAtributoRepository.findByPlantillaNombre(nombrePlantilla);

        List<AtributoFormularioDTO> filtrados = atributos.stream()
                .filter(attr -> !nombresBase.contains(attr.getTipoAtributo().getNombreAtributo()))
                .map(plantillaAtributoMapper::toAtributoFormularioDTO)
                .sorted(Comparator.comparingInt(AtributoFormularioDTO::orden))
                .collect(Collectors.toList());

        List<AtributoFormularioDTO> resultado = new ArrayList<>();
        for (int i = 0; i < filtrados.size(); i++) {
            AtributoFormularioDTO orig = filtrados.get(i);
            resultado.add(new AtributoFormularioDTO(
                    orig.tipoAtributoId(),
                    orig.nombreTipoAtributo(),
                    orig.etiqueta(),
                    orig.tipoDato(),
                    orig.requerido(),
                    orig.valorDefecto(),
                    ORDEN_BASE_DINAMICOS + i,
                    orig.placeholder(),
                    orig.opciones()
            ));
        }
        return resultado;
    }

    private Set<String> extraerNombres(List<AtributoFormularioDTO> campos) {
        return campos.stream()
                .map(AtributoFormularioDTO::nombreTipoAtributo)
                .collect(Collectors.toSet());
    }

    private List<AtributoFormularioDTO> combinarYOrdenar(
            List<AtributoFormularioDTO> base,
            List<AtributoFormularioDTO> dinamicos
    ) {
        List<AtributoFormularioDTO> todos = new ArrayList<>(base.size() + dinamicos.size());
        todos.addAll(base);
        todos.addAll(dinamicos);
        todos.sort(Comparator.comparingInt(AtributoFormularioDTO::orden)
                .thenComparing(AtributoFormularioDTO::etiqueta));
        return todos;
    }


    private record NombreResuelto(String original, String plantilla) {}
}