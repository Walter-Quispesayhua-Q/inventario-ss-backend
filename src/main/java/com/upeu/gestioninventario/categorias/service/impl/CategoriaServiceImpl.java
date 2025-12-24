package com.upeu.gestioninventario.categorias.service.impl;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.categorias.dto.*;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import com.upeu.gestioninventario.categorias.mapper.CategoriaMapper;
import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.categorias.model.CategoriaAtributo;
import com.upeu.gestioninventario.categorias.model.TipoAtributo;
import com.upeu.gestioninventario.categorias.repository.CategoriaAtributoRepository;
import com.upeu.gestioninventario.categorias.repository.CategoriaRepository;
import com.upeu.gestioninventario.categorias.repository.specifications.CategoriaSpecifications;
import com.upeu.gestioninventario.categorias.service.ICategoriaService;
import com.upeu.gestioninventario.inventario.repository.BienRepository;
import com.upeu.gestioninventario.ml.mapper.PlantillaCategoriaMapper;
import com.upeu.gestioninventario.ml.model.PlantillaAtributo;
import com.upeu.gestioninventario.ml.model.PlantillaCategoria;
import com.upeu.gestioninventario.ml.repository.PlantillaCategoriaRepository;
import com.upeu.gestioninventario.ml.service.PlantillaLoaderService;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import com.upeu.gestioninventario.shared.dto.pagination.PaginacionInfo;
import com.upeu.gestioninventario.shared.utils.PaginacionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriaServiceImpl implements ICategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final BienRepository bienRepository;
    private final CategoriaMapper categoriaMapper;
    private final CategoriaAtributoRepository categoriaAtributoRepository;
    private final PlantillaCategoriaMapper plantillaCategoriaMapper;
    private final PlantillaCategoriaRepository plantillaCategoriaRepository;
    private final PlantillaLoaderService plantillaLoaderService;

    @Override
    @Transactional
    public OperacionResultadoDTO<CategoriaDTO> crearCategoria(CategoriaCreacionDTO formData) {
        try {
            String nombreNormalizado = formData.nombreCategoria().trim();
            log.info("Creando categoría: '{}'", nombreNormalizado);

            Optional<Categoria> existente = categoriaRepository
                    .findByNombreCategoriaIgnoreCaseAndFechaEliminacionIsNull(nombreNormalizado);

            CategoriaDTO categoria;
            if (existente.isPresent()) {
                categoria = activarCategoriaExistente(existente.get(), formData);
                log.info("Categoría reactivada exitosamente: {}", nombreNormalizado);
                return OperacionResultadoDTO.exito("Categoría reactivada exitosamente", categoria);
            }

            categoria = crearNuevaCategoria(formData, nombreNormalizado);
            log.info("Categoría creada exitosamente: {}", nombreNormalizado);
            return OperacionResultadoDTO.exito("Categoría creada exitosamente", categoria);

        } catch (Exception e) {
            log.error("Error al crear categoría", e);
            return OperacionResultadoDTO.error("Error al crear la categoría: " + e.getMessage());
        }
    }

    private CategoriaDTO activarCategoriaExistente(Categoria categoria, CategoriaCreacionDTO formData) {
        log.info("Categoría '{}' existe, activando visibilidad", categoria.getNombreCategoria());
        
        categoria.setVisible(true);
        if (formData.descripcion() != null) categoria.setDescripcion(formData.descripcion());
        if (formData.icono() != null) categoria.setIcono(formData.icono());
        if (formData.color() != null) categoria.setColor(formData.color());
        
        categoriaRepository.save(categoria);
        return obtenerCategoriaConDetalles(categoria.getId());
    }

    private CategoriaDTO crearNuevaCategoria(CategoriaCreacionDTO formData, String nombreNormalizado) {
        Categoria nueva = categoriaMapper.toEntity(formData);
        nueva.setVisible(true);
        nueva.setEsInteligente(existePlantillaParaNombre(nombreNormalizado));
        
        Categoria guardada = categoriaRepository.save(nueva);
        log.info("Categoría creada - esInteligente: {}", guardada.getEsInteligente());
        
        return obtenerCategoriaConDetalles(guardada.getId());
    }

    private boolean existePlantillaParaNombre(String nombreCategoria) {
        return !sugerirPlantillas(nombreCategoria).isEmpty();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperacionResultadoDTO<CategoriaDTO> actualizarCategoria(Long id, CategoriaActualizacionDTO updateData) {
        try {
            log.info("Actualizando categoría ID: {}", id);

            Categoria existente = categoriaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

            if (updateData.nombreCategoria() != null && 
                !updateData.nombreCategoria().equals(existente.getNombreCategoria())) {
                Optional<Categoria> duplicada = categoriaRepository.findByNombreCategoria(updateData.nombreCategoria());
                if (duplicada.isPresent()) {
                    log.warn("Intento de actualizar con nombre duplicado: {}", updateData.nombreCategoria());
                    return OperacionResultadoDTO.error("Ya existe una categoría con ese nombre");
                }
            }

            categoriaMapper.updateEntityFromDto(updateData, existente);
            categoriaRepository.save(existente);

            CategoriaDTO actualizada = obtenerCategoriaConDetalles(id);
            log.info("Categoría actualizada exitosamente ID: {}", id);
            return OperacionResultadoDTO.exito("Categoría actualizada exitosamente", actualizada);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar categoría ID {}: {}", id, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al actualizar categoría ID {}", id, e);
            return OperacionResultadoDTO.error("Error al actualizar la categoría");
        }
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<Boolean> eliminarCategoria(Long id) {
        try {
            log.info("Eliminando categoría ID: {}", id);

            Categoria categoria = categoriaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

            if (bienRepository.existsByCategoriaId(id)) {
                log.warn("Intento de eliminar categoría con bienes asociados ID: {}", id);
                return OperacionResultadoDTO.error("No se puede eliminar la categoría porque tiene bienes asociados");
            }

            categoriaRepository.delete(categoria);
            log.info("Categoría eliminada exitosamente: {}", categoria.getNombreCategoria());
            return OperacionResultadoDTO.exito("Categoría eliminada exitosamente", true);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al eliminar categoría ID {}: {}", id, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al eliminar categoría ID {}", id, e);
            return OperacionResultadoDTO.error("Error al eliminar la categoría");
        }
    }

    // CONSULTAS
    @Override
    @Transactional(readOnly = true)
    public CategoriasPaginadas obtenerCategoriasPaginadas(
            Integer page,
            Integer limit,
            Boolean estado,
            String ordenarPor,
            String orden,
            String buscar
    ) {
        log.info("Obteniendo categorías paginadas - página: {}, límite: {}", page, limit);

        Pageable pageable = crearPageable(page, limit, ordenarPor, orden);
        Specification<Categoria> spec = construirSpecification(estado, buscar);

        Page<Categoria> pageResult = categoriaRepository.findAll(spec, pageable);

        List<CategoriaDTO> categorias = pageResult.getContent().stream()
                .map(this::convertirADTOConConteo)
                .collect(Collectors.toList());

        PaginacionInfo paginacion = PaginacionUtils.createPaginacionInfo(pageResult);

        log.info("Categorías: {} de {} total", categorias.size(), pageResult.getTotalElements());
        return CategoriasPaginadas.of(categorias, paginacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaSimpleDTO> obtenerCategoriasSimples() {
        return categoriaRepository.findAll().stream()
                .filter(c -> c.getFechaEliminacion() == null)
                .map(categoriaMapper::toSimpleDto)
                .toList();
    }

    // PLANTILLAS
    @Override
    @Transactional(readOnly = true)
    public List<PlantillaCategoriaDTO> sugerirPlantillas(String nombreCategoria) {
        log.info("Buscando plantillas para: '{}'", nombreCategoria);

        if (nombreCategoria == null || nombreCategoria.isBlank()) {
            return List.of();
        }

        Set<String> palabrasBusqueda = extraerPalabrasNormalizadas(nombreCategoria);
        if (palabrasBusqueda.isEmpty()) {
            return List.of();
        }

        List<PlantillaCategoriaDTO> sugerencias = plantillaLoaderService.getPlantillasCache().values().stream()
                .filter(plantilla -> coincidePorPalabrasClave(plantilla, palabrasBusqueda))
                .map(p -> plantillaCategoriaRepository.findByIdPlantillaSeed(p.metadatos().idPlantilla()))
                .flatMap(Optional::stream)
                .map(plantillaCategoriaMapper::toDto)
                .collect(Collectors.toList());

        log.info("Encontradas {} plantillas para '{}'", sugerencias.size(), nombreCategoria);
        return sugerencias;
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<CategoriaDTO> aplicarPlantilla(Long categoriaId, Long plantillaId) {
        try {
            log.info("Aplicando plantilla {} a categoría {}", plantillaId, categoriaId);

            Categoria categoria = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + categoriaId));

            PlantillaCategoria plantilla = plantillaCategoriaRepository.findByIdWithAtributos(plantillaId)
                    .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada con ID: " + plantillaId));

            categoria.setEsInteligente(true);

            Set<Long> atributosExistentes = obtenerIdsAtributosExistentes(categoriaId);
            int atributosAgregados = agregarAtributosDePlantilla(categoria, plantilla, atributosExistentes);

            categoriaRepository.save(categoria);

            CategoriaDTO categoriaActualizada = obtenerCategoriaConDetalles(categoriaId);
            log.info("Plantilla aplicada exitosamente. {} atributos agregados", atributosAgregados);
            return OperacionResultadoDTO.exito("Plantilla aplicada exitosamente a la categoría", categoriaActualizada);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al aplicar plantilla: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al aplicar plantilla", e);
            return OperacionResultadoDTO.error("Error al aplicar la plantilla");
        }
    }

    // MÉTODOS AUXILIARES

    // MÉTODOS AUXILIARES - TRANSFORMACIÓN
    private CategoriaDTO obtenerCategoriaConDetalles(Long id) {
        return categoriaRepository.findByIdWithDetails(id)
                .map(categoriaMapper::toDto)
                .orElseThrow(() -> new IllegalStateException("Error al cargar categoría: " + id));
    }

    private CategoriaDTO convertirADTOConConteo(Categoria categoria) {
        Integer cantidadBienes = categoriaRepository.countBienesPorCategoria(categoria.getId());

        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNombreCategoria(),
                categoria.getDescripcion(),
                categoria.getFechaCreacion(),
                obtenerNombreUsuario(categoria.getUsuarioCreacion()),
                categoria.getFechaUltimaModificacion(),
                obtenerNombreUsuario(categoria.getUsuarioUltimaModificacion()),
                categoria.getIcono(),
                categoria.getColor(),
                categoria.getEstado(),
                categoria.getEsInteligente(),
                cantidadBienes != null ? cantidadBienes : 0
        );
    }

    private String obtenerNombreUsuario(Usuario usuario) {
        if (usuario == null || usuario.getPersona() == null) return null;
        return usuario.getPersona().getNombre();
    }

    // MÉTODOS AUXILIARES - PAGINACIÓN Y ESPECIFICACIONES
    private Pageable crearPageable(Integer page, Integer limit, String ordenarPor, String orden) {
        String sortField = (ordenarPor != null && !ordenarPor.isEmpty()) ? ordenarPor : "nombreCategoria";
        return PaginacionUtils.createPageable(page, limit, sortField, orden);
    }

    private Specification<Categoria> construirSpecification(Boolean estado, String buscar) {
        return CategoriaSpecifications.noEliminado()
                .and(CategoriaSpecifications.soloVisibles())
                .and(CategoriaSpecifications.conEstado(estado))
                .and(CategoriaSpecifications.buscarPorNombreODescripcion(buscar));
    }


    // MÉTODOS AUXILIARES - PLANTILLAS
    private Set<String> extraerPalabrasNormalizadas(String texto) {
        if (texto == null || texto.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(texto.toLowerCase().split("\\s+"))
                .map(p -> p.replaceAll("es$", "").replaceAll("s$", ""))
                .filter(p -> p.length() > 2)
                .collect(Collectors.toSet());
    }

    private boolean coincidePorPalabrasClave(PlantillaSeedDTO plantilla, Set<String> palabrasBusqueda) {
        Set<String> palabrasPlantilla = extraerPalabrasClaveDeReconocimiento(plantilla);
        return !Collections.disjoint(palabrasBusqueda, palabrasPlantilla);
    }

    @SuppressWarnings("unchecked")
    private Set<String> extraerPalabrasClaveDeReconocimiento(PlantillaSeedDTO plantilla) {
        Set<String> palabras = new HashSet<>();

        if (plantilla.reconocimiento() == null) return palabras;

        Object pc = plantilla.reconocimiento().get("palabrasClave");
        if (!(pc instanceof Map)) return palabras;

        Map<String, Object> palabrasClaveMap = (Map<String, Object>) pc;

        for (Object valor : palabrasClaveMap.values()) {
            if (valor instanceof Map) {
                Object terminos = ((Map<String, Object>) valor).get("terminos");
                if (terminos instanceof List) {
                    ((List<String>) terminos).stream()
                            .map(String::toLowerCase)
                            .forEach(palabras::add);
                }
            }
        }
        return palabras;
    }

    private Set<Long> obtenerIdsAtributosExistentes(Long categoriaId) {
        return categoriaAtributoRepository.findByCategoriaId(categoriaId).stream()
                .map(ca -> ca.getTipoAtributo().getId())
                .collect(Collectors.toSet());
    }

    private int agregarAtributosDePlantilla(
            Categoria categoria,
            PlantillaCategoria plantilla,
            Set<Long> atributosExistentes
    ) {
        int agregados = 0;

        for (PlantillaAtributo pa : plantilla.getPlantillaAtributos()) {
            TipoAtributo tipo = pa.getTipoAtributo();
            if (tipo == null || atributosExistentes.contains(tipo.getId())) {
                continue;
            }

            CategoriaAtributo enlace = CategoriaAtributo.builder()
                    .categoria(categoria)
                    .tipoAtributo(tipo)
                    .etiqueta(pa.getEtiqueta() != null ? pa.getEtiqueta() : generarEtiqueta(tipo.getNombreAtributo()))
                    .requerido(pa.getEsRequerido() != null ? pa.getEsRequerido() : true)
                    .build();

            categoriaAtributoRepository.save(enlace);
            agregados++;
        }
        return agregados;
    }

    private String generarEtiqueta(String nombreAtributo) {
        if (nombreAtributo == null || nombreAtributo.isEmpty()) {
            return "";
        }
        String etiqueta = nombreAtributo.replace("_", " ");
        return Character.toUpperCase(etiqueta.charAt(0)) + etiqueta.substring(1).toLowerCase();
    }
}