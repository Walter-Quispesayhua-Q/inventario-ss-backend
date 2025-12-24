package com.upeu.gestioninventario.inventario.service.impl;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.categorias.repository.CategoriaRepository;
import com.upeu.gestioninventario.inventario.dto.KeyValueInput;
import com.upeu.gestioninventario.inventario.dto.bien.*;
import com.upeu.gestioninventario.inventario.mapper.BienMapper;
import com.upeu.gestioninventario.inventario.model.Bien;
import com.upeu.gestioninventario.inventario.repository.BienRepository;
import com.upeu.gestioninventario.inventario.repository.specifications.BienSpecifications;
import com.upeu.gestioninventario.inventario.service.IBienPersistenciaService;
import com.upeu.gestioninventario.inventario.service.IBienService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import com.upeu.gestioninventario.shared.utils.PaginacionUtils;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import com.upeu.gestioninventario.ubicaciones.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BienServiceImpl implements IBienService {

    private final BienRepository bienRepository;
    private final BienMapper bienMapper;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UbicacionService ubicacionService;
    private final IBienPersistenciaService bienPersistenciaService;

    // CREACIÓN (delega a BienPersistenciaService)
    @Override
    @Transactional
    public OperacionResultadoDTO<BienDTO> crearBien(BienCreacionInput input) {
        try {
            log.info("Creando bien desde formulario: {}", input.campos() != null ? input.campos().size() + " campos" : "sin campos");

            Categoria categoria = resolverCategoria(input.categoriaId(), input.nombreNuevaCategoria());
            Usuario usuario = resolverResponsable(input.responsableActualId());
            Ubicacion ubicacion = ubicacionService.creacionUbicacion(input.ubicacion());

            Map<String, String> campos = toMap(input.campos());

            BienDTO bienCreado = bienPersistenciaService.guardarBien(
                    campos, categoria, usuario.getPersona(), ubicacion, usuario.getDepartamento()
            );

            log.info("Bien creado exitosamente: {} (CAF: {})", bienCreado.nombreBien(), bienCreado.caf());
            return OperacionResultadoDTO.exito("Bien creado exitosamente", bienCreado);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear bien: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al crear bien", e);
            return OperacionResultadoDTO.error("Error al crear el bien");
        }
    }


    // ACTUALIZACIÓN (delega a BienPersistenciaService)
    @Override
    @Transactional
    public OperacionResultadoDTO<BienDTO> actualizarBien(Long id, BienActualizacionInput input) {
        try {
            log.info("Actualizando bien ID: {}", id);

            Categoria categoria = null;
            if (tieneCategoria(input)) {
                categoria = resolverCategoria(input.categoriaId(), input.nombreNuevaCategoria());
            }

            Ubicacion ubicacion = null;
            if (input.ubicacion() != null) {
                ubicacion = ubicacionService.creacionUbicacion(input.ubicacion());
            }

            // Actualizar responsable directamente si se proporciona
            if (input.responsableActualId() != null) {
                Bien bien = buscarPorId(id);
                Usuario usuario = resolverResponsable(input.responsableActualId());
                bien.setResponsableActual(usuario.getPersona());
                bien.setDepartamento(usuario.getDepartamento());
                bienRepository.save(bien);
            }

            Map<String, String> campos = input.campos() != null ? toMap(input.campos()) : Map.of();
            BienDTO bienActualizado = bienPersistenciaService.actualizarBienExistente(id, campos, categoria, ubicacion);

            log.info("Bien actualizado exitosamente ID: {}", id);
            return OperacionResultadoDTO.exito("Bien actualizado exitosamente", bienActualizado);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar bien ID {}: {}", id, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al actualizar bien ID {}", id, e);
            return OperacionResultadoDTO.error("Error al actualizar el bien");
        }
    }

    // CONSULTAS
    @Override
    @Transactional(readOnly = true)
    public BienDTO obtenerBienPorId(Long id) {
        return bienMapper.toDto(buscarPorId(id));
    }


    @Override
    @Transactional(readOnly = true)
    public BienesPaginados obtenerBienesPaginados(
            Integer page, Integer limit, String buscar,
            Long categoriaId, Long ubicacionId,
            String estadoOperacional, String estadoFisico,
            Long departamentoId, Long responsableId,
            String ordenarPor, String orden
    ) {
        Pageable pageable = crearPageable(page, limit, ordenarPor, orden);
        Specification<Bien> spec = construirSpec(buscar, categoriaId, ubicacionId,
                estadoOperacional, estadoFisico, departamentoId, responsableId);

        Page<Bien> result = bienRepository.findAll(spec, pageable);
        List<BienDTO> bienes = result.getContent().stream().map(bienMapper::toDto).collect(Collectors.toList());

        return BienesPaginados.of(bienes, PaginacionUtils.createPaginacionInfo(result));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienDTO> buscarBienes(String termino) {
        return bienRepository.buscarBienesIndividuales(termino).stream()
                .map(bienMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienDTO> obtenerBienesPorCategoria(Long categoriaId) {
        return bienRepository.findBienesIndividualesByCategoriaId(categoriaId).stream()
                .map(bienMapper::toDto).collect(Collectors.toList());
    }

    // ELIMINACIÓN
    @Override
    @Transactional
    public OperacionResultadoDTO<BienEliminacionResultadoDTO> eliminarBien(Long id) {
        try {
            log.info("Eliminando bien ID: {}", id);
            
            Bien bien = buscarPorId(id);
            String nombreBien = bien.getNombreBien();
            
            bienRepository.delete(bien);
            
            log.info("Bien eliminado exitosamente: {} (ID: {})", nombreBien, id);
            BienEliminacionResultadoDTO resultado = new BienEliminacionResultadoDTO(true, "Bien eliminado exitosamente");
            return OperacionResultadoDTO.exito("Bien eliminado exitosamente", resultado);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al eliminar bien ID {}: {}", id, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al eliminar bien ID {}", id, e);
            return OperacionResultadoDTO.error("Error al eliminar el bien");
        }
    }


    // RESOLUCIÓN DE ENTIDADES
    private Bien buscarPorId(Long id) {
        return bienRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Bien no encontrado: " + id));
    }

    private Categoria resolverCategoria(Long id, String nombre) {
        if (id != null) {
            Categoria categoria = categoriaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));
            return activarSiNoVisible(categoria);
        }
        if (nombre != null && !nombre.isBlank()) {
            return categoriaRepository.findByNombreCategoria(nombre)
                    .map(this::activarSiNoVisible)
                    .orElseGet(() -> categoriaRepository.save(
                            Categoria.builder()
                                    .nombreCategoria(nombre)
                                    .estado(true)
                                    .visible(true)
                                    .esInteligente(false)
                                    .build()
                    ));
        }
        throw new IllegalArgumentException("Debe proporcionar categoriaId o nombreNuevaCategoria");
    }

    private Categoria activarSiNoVisible(Categoria categoria) {
        if (!Boolean.TRUE.equals(categoria.getVisible())) {
            categoria.setVisible(true);
            return categoriaRepository.save(categoria);
        }
        return categoria;
    }

    private Usuario resolverResponsable(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Responsable no encontrado: " + id));
        if (u.getDepartamento() == null) {
            throw new IllegalStateException("Responsable sin departamento: " + id);
        }
        return u;
    }

    // UTILIDADES
    private boolean tieneCategoria(BienActualizacionInput i) {
        return i.categoriaId() != null || (i.nombreNuevaCategoria() != null && !i.nombreNuevaCategoria().isBlank());
    }

    private Map<String, String> toMap(List<KeyValueInput> c) {
        return c.stream().collect(Collectors.toMap(KeyValueInput::clave, KeyValueInput::valor, (a, b) -> b));
    }

    private Pageable crearPageable(Integer p, Integer l, String o, String d) {
        return PaginacionUtils.createPageable(p, l, o != null && !o.isEmpty() ? o : "fechaCreacion", d);
    }

    private Specification<Bien> construirSpec(
            String buscar, Long categoriaId, Long ubicacionId,
            String estadoOperacional, String estadoFisico,
            Long departamentoId, Long responsableId) {

        return BienSpecifications.soloActivosNoEliminados()
                .and(BienSpecifications.buscarEnMultiplesCampos(buscar))
                .and(BienSpecifications.porCategoriaId(categoriaId))
                .and(BienSpecifications.porUbicacionId(ubicacionId))
                .and(BienSpecifications.porEstadoOperacional(estadoOperacional))
                .and(BienSpecifications.porEstadoFisico(estadoFisico))
                .and(BienSpecifications.porDepartamentoId(departamentoId))
                .and(BienSpecifications.porResponsableId(responsableId));
    }
}