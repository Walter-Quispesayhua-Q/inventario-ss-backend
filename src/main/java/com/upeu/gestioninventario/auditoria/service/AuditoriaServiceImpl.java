package com.upeu.gestioninventario.auditoria.service;

import com.upeu.gestioninventario.auditoria.dto.RegistroActividadCreacionInput;
import com.upeu.gestioninventario.auditoria.dto.RegistroActividadDTO;
import com.upeu.gestioninventario.auditoria.dto.RegistrosActividadPaginados;
import com.upeu.gestioninventario.auditoria.mapper.RegistroActividadMapper;
import com.upeu.gestioninventario.auditoria.model.RegistroActividad;
import com.upeu.gestioninventario.auditoria.model.TipoEndpoint;
import com.upeu.gestioninventario.auditoria.model.TipoOperacion;
import com.upeu.gestioninventario.auditoria.repository.RegistroActividadRepository;
import com.upeu.gestioninventario.auditoria.repository.specifications.RegistroActividadSpecifications;
import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.shared.dto.pagination.PaginacionInfo;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import com.upeu.gestioninventario.shared.utils.PaginacionUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaServiceImpl implements IAuditoriaService {

    private final RegistroActividadRepository registroActividadRepository;
    private final RegistroActividadMapper registroActividadMapper;
    private final UsuarioRepository usuarioRepository;
    private final EntityManager entityManager;

    @Async
    @Transactional
    public void registrarActividad(
            TipoOperacion tipoOperacion,
            TipoEndpoint tipoEndpoint,
            String entidadAfectada,
            Long entidadId,
            String descripcion,
            Map<String, Object> metadatos,
            String ipAddress,
            String userAgent,
            Usuario usuario
    ) {
        try {
            log.debug("Registrando actividad: {} - {} - {} para usuario: {}", 
                     tipoOperacion, entidadAfectada, descripcion, 
                     usuario != null ? usuario.getId() : "Sistema");

            if (usuario == null) {
                try {
                    if (metadatos != null) {
                        Object possible = metadatos.get("userId");
                        if (possible == null) possible = metadatos.get("usuarioId");
                        if (possible == null) possible = metadatos.get("idUsuario");
                        if (possible instanceof Number) {
                            Long id = ((Number) possible).longValue();
                            usuario = obtenerUsuarioPorId(id);
                        } else if (possible instanceof String) {
                            try {
                                Long id = Long.parseLong((String) possible);
                                usuario = obtenerUsuarioPorId(id);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                } catch (Exception e) {
                    log.trace("Error al buscar userId en metadatos: {}", e.getMessage());
                }

                if (usuario == null && descripcion != null) {
                    try {
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile("ID\\s*[:\\-]?\\s*(\\d+)");
                        java.util.regex.Matcher m = p.matcher(descripcion);
                        if (m.find()) {
                            try {
                                Long id = Long.parseLong(m.group(1));
                                usuario = obtenerUsuarioPorId(id);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } catch (Exception e) {
                        log.trace("Error al parsear ID desde descripcion: {}", e.getMessage());
                    }
                }

                if (usuario == null && entidadId != null) {
                    try {
                        Usuario u = obtenerUsuarioPorId(entidadId);
                        if (u != null) {
                            usuario = u;
                        }
                    } catch (Exception e) {
                        log.trace("No se pudo obtener usuario por entidadId fallback: {}", e.getMessage());
                    }
                }
            }

            // Antes de persistir, si tenemos un objeto Usuario intentar recargarlo para que sea managed en esta transacción
            if (usuario != null) {
                try {
                    Usuario managed = usuarioRepository.findById(usuario.getId()).orElse(null);
                    if (managed != null) {
                        usuario = managed;
                    } else {
                        // Si no lo encontramos, dejamos el objeto tal cual (posiblemente contiene solo id/email)
                        log.debug("Usuario pasado a registrarActividad no encontrado en BD al revalidar: {}", usuario.getId());
                    }
                } catch (Exception e) {
                    log.trace("No se pudo recargar usuario en registrarActividad: {}", e.getMessage());
                }
            }

            RegistroActividad registro = RegistroActividad.builder()
                    .tipoOperacion(tipoOperacion)
                    .tipoEndpoint(tipoEndpoint)
                    .entidadAfectada(entidadAfectada)
                    .entidadId(entidadId)
                    .descripcion(descripcion)
                    .metadatos(metadatos)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .fechaOperacion(OffsetDateTime.now(ZoneOffset.UTC))
                    .usuario(usuario)
                    .build();

            registroActividadRepository.save(registro);
            log.info("Actividad registrada exitosamente: ID {} para usuario: {}", 
                    registro.getId(), usuario != null ? usuario.getId() : "Sistema");

        } catch (Exception e) {
            log.error("Error al registrar actividad: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<RegistroActividadDTO> crearRegistroManual(RegistroActividadCreacionInput input) {
        try {
            log.info("Creando registro de actividad manual: {}", input.descripcion());

            Usuario usuario = obtenerUsuarioActual();

            RegistroActividad registro = registroActividadMapper.toEntity(input);
            registro.setFechaOperacion(OffsetDateTime.now(ZoneOffset.UTC));
            registro.setUsuario(usuario);

            RegistroActividad registroGuardado = registroActividadRepository.save(registro);
            RegistroActividadDTO resultado = registroActividadMapper.toDto(registroGuardado, entityManager);
            
            log.info("Registro de actividad creado exitosamente: ID {}", registroGuardado.getId());
            return OperacionResultadoDTO.exito("Registro de actividad creado exitosamente", resultado);

        } catch (Exception e) {
            log.error("Error al crear registro de actividad manual", e);
            return OperacionResultadoDTO.error("Error al crear el registro de actividad");
        }
    }

    @Transactional(readOnly = true)
    public RegistrosActividadPaginados obtenerRegistrosPaginados(
            Integer page,
            Integer limit,
            String buscar,
            Long usuarioId,
            String tipoOperacion,
            String entidadAfectada,
            Long entidadId,
            OffsetDateTime fechaInicio,
            OffsetDateTime fechaFin,
            String ordenarPor,
            String orden
    ) {
        log.info("Obteniendo registros de actividad paginados - página: {}, límite: {}", page, limit);

        Pageable pageable = PaginacionUtils.createPageable(page, limit, ordenarPor, orden);

        Specification<RegistroActividad> spec = RegistroActividadSpecifications.conUsuarioYPersona()
                .and(RegistroActividadSpecifications.soloActivosNoEliminados());

        if (buscar != null && !buscar.isBlank()) {
            spec = spec.and(RegistroActividadSpecifications.buscarEnMultiplesCampos(buscar));
        }
        if (usuarioId != null) {
            spec = spec.and(RegistroActividadSpecifications.porUsuarioId(usuarioId));
        }
        if (tipoOperacion != null && !tipoOperacion.isBlank()) {
            spec = spec.and(RegistroActividadSpecifications.porTipoOperacion(tipoOperacion));
        }
        if (entidadAfectada != null && !entidadAfectada.isBlank()) {
            spec = spec.and(RegistroActividadSpecifications.porEntidadAfectada(entidadAfectada));
        }
        if (entidadId != null) {
            spec = spec.and(RegistroActividadSpecifications.porEntidadId(entidadId));
        }
        if (fechaInicio != null || fechaFin != null) {
            java.time.LocalDateTime inicioLocal = fechaInicio != null ? fechaInicio.toLocalDateTime() : null;
            java.time.LocalDateTime finLocal = fechaFin != null ? fechaFin.toLocalDateTime() : null;
            spec = spec.and(RegistroActividadSpecifications.porRangoFechas(inicioLocal, finLocal));
        }

        Page<RegistroActividad> pageResult = registroActividadRepository.findAll(spec, pageable);

        List<RegistroActividadDTO> registrosDTO = pageResult.getContent().stream()
                .map(registro -> registroActividadMapper.toDto(registro, entityManager))
                .collect(Collectors.toList());

        PaginacionInfo paginacion = PaginacionUtils.createPaginacionInfo(pageResult);

        log.info("Se encontraron {} registros de actividad en {} páginas",
                paginacion.getTotalElementos(), paginacion.getTotalPaginas());

        return new RegistrosActividadPaginados(registrosDTO, paginacion);
    }

    @Transactional(readOnly = true)
    public RegistroActividadDTO obtenerRegistroPorId(Long id) {
        log.info("Obteniendo registro de actividad con ID: {}", id);

        RegistroActividad registro = registroActividadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro de actividad no encontrado con ID: " + id));

        return registroActividadMapper.toDto(registro, entityManager);
    }

    @Transactional(readOnly = true)
    public List<RegistroActividadDTO> obtenerActividadesPorUsuario(Long usuarioId) {
        log.info("Obteniendo actividades del usuario con ID: {}", usuarioId);

        List<RegistroActividad> registros = registroActividadRepository
                .findByUsuarioIdOrderByFechaOperacionDesc(usuarioId);

        return registros.stream()
                .map(registro -> registroActividadMapper.toDto(registro, entityManager))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistroActividadDTO> obtenerActividadesPorEntidad(String entidadAfectada, Long entidadId) {
        log.info("Obteniendo actividades para entidad: {} con ID: {}", entidadAfectada, entidadId);

        List<RegistroActividad> registros = registroActividadRepository
                .findByEntidadAfectadaAndEntidadIdOrderByFechaOperacionDesc(entidadAfectada, entidadId);

        return registros.stream()
                .map(registro -> registroActividadMapper.toDto(registro, entityManager))
                .collect(Collectors.toList());
    }

    public Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("No hay usuario autenticado, retornando null");
            return null;
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        if (usuario != null) {
            log.debug("Usuario actual obtenido: {} (ID: {})", email, usuario.getId());
        } else {
            log.warn("No se encontró usuario con email: {}", email);
        }
        
        return usuario;
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return usuarioRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.warn("Error al obtener usuario por ID {}: {}", id, e.getMessage());
            return null;
        }
    }

}
