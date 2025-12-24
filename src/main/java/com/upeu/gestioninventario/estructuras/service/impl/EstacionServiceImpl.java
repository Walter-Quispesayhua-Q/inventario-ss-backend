package com.upeu.gestioninventario.estructuras.service.impl;

import com.upeu.gestioninventario.estructuras.dto.BienesParaEstacionResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.BienAsignadoDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.BienEstacionPaginadoDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionInputDTO;
import com.upeu.gestioninventario.estructuras.mapper.BienEstacionMapper;
import com.upeu.gestioninventario.estructuras.mapper.EstacionMapper;
import com.upeu.gestioninventario.estructuras.model.*;
import com.upeu.gestioninventario.estructuras.repository.AmbienteRepository;
import com.upeu.gestioninventario.estructuras.repository.BienEstacionRepository;
import com.upeu.gestioninventario.estructuras.repository.EstacionRepository;
import com.upeu.gestioninventario.estructuras.repository.TipoEstructuraRepository;
import com.upeu.gestioninventario.estructuras.service.IEstacionService;
import com.upeu.gestioninventario.inventario.model.Bien;
import com.upeu.gestioninventario.personas.repository.PersonaRepository;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EstacionServiceImpl implements IEstacionService {

    private final EstacionRepository estacionRepository;
    private final AmbienteRepository ambienteRepository;
    private final TipoEstructuraRepository tipoEstructuraRepository;
    private final BienEstacionRepository bienEstacionRepository;
    private final PersonaRepository personaRepository;
    private final EstacionMapper estacionMapper;
    private final BienEstacionMapper bienEstacionMapper;


    @Override
    public OperacionResultadoDTO<EstacionDTO> crearEstacion(EstacionInputDTO inputDTO) {
        try {
            log.info("Creando estación: {} en ambiente ID: {}", inputDTO.getNombre(), inputDTO.getIdAmbiente());

            if (inputDTO.getIdAmbiente() == null) {
                return OperacionResultadoDTO.error("El ambiente es obligatorio para crear una estación");
            }

            Ambiente ambiente = ambienteRepository.findById(inputDTO.getIdAmbiente())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ambiente no encontrado con ID: " + inputDTO.getIdAmbiente()));

            String codigoFinal = generarCodigoEstacion(inputDTO.getCodigo(), ambiente);

            if (codigoFinal != null && estacionRepository.existsByAmbienteIdAmbienteAndCodigo(
                    inputDTO.getIdAmbiente(), codigoFinal)) {
                return OperacionResultadoDTO.error(
                        "Ya existe una estación con código '" + codigoFinal + "' en '" + ambiente.getNombre() + "'");
            }

            TipoEstructuraEntity tipoEstructura = tipoEstructuraRepository.findById(inputDTO.getIdTipoEstructura())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de estructura no encontrado"));

            if (!tipoEstructura.getActivo()) {
                return OperacionResultadoDTO.error("El tipo de estructura no está activo");
            }

            Estacion estacion = estacionMapper.toEntity(inputDTO);
            estacion.setCodigo(codigoFinal);
            estacion.setAmbiente(ambiente);
            estacion.setTipoEstructura(tipoEstructura);
            estacion.setCapacidadBienes(inputDTO.getCapacidadBienes() != null
                    ? inputDTO.getCapacidadBienes()
                    : calcularCapacidadBienes(tipoEstructura));

            if (inputDTO.getIdResponsable() != null) {
                personaRepository.findById(inputDTO.getIdResponsable())
                        .ifPresent(estacion::setResponsable);
            }

            Estacion estacionGuardada = estacionRepository.save(estacion);
            log.info("Estación creada: {} (ID: {})", estacionGuardada.getNombre(), estacionGuardada.getIdEstacion());

            return OperacionResultadoDTO.exito("Estación creada exitosamente", 
                    obtenerDTOConEstadisticas(estacionGuardada));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error al crear estación", e);
            return OperacionResultadoDTO.error("Error al crear la estación");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EstacionDTO obtenerEstacionPorId(Long idEstacion) {
        log.info("Obteniendo estación ID: {}", idEstacion);
        return obtenerDTOConEstadisticas(buscarEstacionPorId(idEstacion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstacionDTO> listarEstacionesPorAmbiente(Long idAmbiente) {
        log.info("Listando estaciones del ambiente ID: {}", idAmbiente);

        if (!ambienteRepository.existsById(idAmbiente)) {
            throw new RuntimeException("Ambiente no encontrado con ID: " + idAmbiente);
        }

        return obtenerDTOsConEstadisticas(estacionRepository.findByAmbienteWithDetails(idAmbiente));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstacionDTO> listarTodasEstaciones() {
        log.info("Listando todas las estaciones");
        return obtenerDTOsConEstadisticas(estacionRepository.findAll());
    }


    @Override
    @Transactional(readOnly = true)
    public List<EstacionDTO> obtenerEstacionesConCapacidadDisponible() {
        log.info("Obteniendo estaciones con capacidad disponible");
        return obtenerDTOsConEstadisticas(estacionRepository.findEstacionesConCapacidadDisponible());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstacionDTO> buscarEstacionesConFiltros(FiltroEstacionDTO filtro, OrdenEstacion orden) {
        log.info("Buscando estaciones con filtros: {}", filtro);

        List<Estacion> estaciones;

        if (filtro.getCodigoBien() != null && !filtro.getCodigoBien().isBlank()) {
            estaciones = estacionRepository.buscarPorCodigoBien(filtro.getCodigoBien().trim());
        } else if (filtro.getTieneBienesAsignados() != null) {
            estaciones = filtro.getTieneBienesAsignados()
                    ? estacionRepository.findEstacionesConBienesAsignados()
                    : estacionRepository.findEstacionesSinBienesAsignados();
        } else {
            estaciones = estacionRepository.buscarConFiltros(
                    filtro.getNombre(),
                    filtro.getCodigo(),
                    filtro.getIdAmbiente(),
                    filtro.getIdPiso(),
                    filtro.getIdEdificio(),
                    filtro.getIdTipoEstructura()
            );
        }

        return obtenerDTOsConEstadisticas(estaciones);
    }

    @Override
    @Transactional(readOnly = true)
    public BienEstacionPaginadoDTO obtenerBienesDeEstacionPaginados(Long idEstacion, int pagina, int tamanoPagina) {
        log.info("Obteniendo bienes de estación {} - Página: {}", idEstacion, pagina);

        if (!estacionRepository.existsById(idEstacion)) {
            throw new RuntimeException("Estación no encontrada con ID: " + idEstacion);
        }

        Page<BienEstacion> bienesPage = bienEstacionRepository.findByEstacionIdEstacionAndAsignacionActiva(
                idEstacion, true, PageRequest.of(pagina, tamanoPagina));

        return BienEstacionPaginadoDTO.builder()
                .contenido(bienEstacionMapper.toDTOList(bienesPage.getContent()))
                .totalElementos((int) bienesPage.getTotalElements())
                .totalPaginas(bienesPage.getTotalPages())
                .paginaActual(bienesPage.getNumber())
                .tamanoPagina(bienesPage.getSize())
                .primeraPagina(bienesPage.isFirst())
                .ultimaPagina(bienesPage.isLast())
                .build();
    }

    @Override
    public OperacionResultadoDTO<EstacionDTO> actualizarEstacion(Long idEstacion, EstacionInputDTO inputDTO) {
        try {
            log.info("Actualizando estación ID: {}", idEstacion);

            Estacion estacion = estacionRepository.findById(idEstacion)
                    .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada con ID: " + idEstacion));

            Long idAmbienteActual = estacion.getAmbiente().getIdAmbiente();
            Long idAmbienteNuevo = inputDTO.getIdAmbiente() != null ? inputDTO.getIdAmbiente() : idAmbienteActual;

            if (inputDTO.getCodigo() != null && !inputDTO.getCodigo().equals(estacion.getCodigo())) {
                if (estacionRepository.existsByAmbienteIdAmbienteAndCodigo(idAmbienteNuevo, inputDTO.getCodigo())) {
                    return OperacionResultadoDTO.error("Ya existe una estación con código: " + inputDTO.getCodigo());
                }
            }

            if (inputDTO.getIdAmbiente() != null && !inputDTO.getIdAmbiente().equals(idAmbienteActual)) {
                Ambiente ambienteNuevo = ambienteRepository.findById(inputDTO.getIdAmbiente())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Ambiente no encontrado con ID: " + inputDTO.getIdAmbiente()));
                estacion.setAmbiente(ambienteNuevo);
            }

            if (inputDTO.getIdTipoEstructura() != null) {
                TipoEstructuraEntity tipoEstructura = tipoEstructuraRepository.findById(inputDTO.getIdTipoEstructura())
                        .orElseThrow(() -> new IllegalArgumentException("Tipo de estructura no encontrado"));

                if (!tipoEstructura.getActivo()) {
                    return OperacionResultadoDTO.error("El tipo de estructura no está activo");
                }

                // Solo recalcular capacidad si el tipo de estructura CAMBIÓ y NO se envió capacidad manual
                Long idTipoActual = estacion.getTipoEstructura() != null 
                        ? estacion.getTipoEstructura().getIdTipo() 
                        : null;
                boolean tipoEstructuraCambio = !inputDTO.getIdTipoEstructura().equals(idTipoActual);

                estacion.setTipoEstructura(tipoEstructura);

                // Solo recalcular si cambió el tipo Y no se envió capacidad manual
                if (tipoEstructuraCambio && inputDTO.getCapacidadBienes() == null) {
                    Integer nuevaCapacidad = calcularCapacidadBienes(tipoEstructura);
                    long bienesActuales = contarBienesAsignadosActivos(idEstacion);

                    if (nuevaCapacidad < bienesActuales) {
                        return OperacionResultadoDTO.error(String.format(
                                "No se puede cambiar a tipo '%s' (capacidad: %d) porque la estación tiene %d bienes asignados. " +
                                "Desasigne algunos bienes primero.",
                                tipoEstructura.getNombre(), nuevaCapacidad, bienesActuales));
                    }

                    estacion.setCapacidadBienes(nuevaCapacidad);
                    log.info("Tipo de estructura cambió, nueva capacidad calculada: {}", nuevaCapacidad);
                }
            }

            // Validar capacidadBienes si se envió desde el frontend
            if (inputDTO.getCapacidadBienes() != null) {
                long bienesActuales = contarBienesAsignadosActivos(idEstacion);
                if (inputDTO.getCapacidadBienes() < bienesActuales) {
                    return OperacionResultadoDTO.error(String.format(
                            "No se puede establecer capacidad %d porque la estación tiene %d bienes asignados.",
                            inputDTO.getCapacidadBienes(), bienesActuales));
                }
                log.info("Capacidad manual establecida: {}", inputDTO.getCapacidadBienes());
            }

            estacionMapper.updateEntityFromInput(inputDTO, estacion);
            Estacion estacionActualizada = estacionRepository.save(estacion);

            log.info("Estación actualizada ID: {}", idEstacion);
            return OperacionResultadoDTO.exito("Estación actualizada exitosamente", 
                    obtenerDTOConEstadisticas(estacionActualizada));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error al actualizar estación ID {}", idEstacion, e);
            return OperacionResultadoDTO.error("Error al actualizar la estación");
        }
    }

    @Override
    public OperacionResultadoDTO<Boolean> eliminarEstacion(Long idEstacion) {
        try {
            log.info("Eliminando estación ID: {}", idEstacion);

            Estacion estacion = estacionRepository.findById(idEstacion)
                    .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada con ID: " + idEstacion));

            long bienesAsignados = contarBienesAsignadosActivos(idEstacion);
            if (bienesAsignados > 0) {
                return OperacionResultadoDTO.error(String.format(
                        "No se puede eliminar: tiene %d bien(es) asignado(s)", bienesAsignados));
            }

            estacionRepository.delete(estacion);
            log.info("Estación eliminada: {}", estacion.getNombre());
            return OperacionResultadoDTO.exito("Estación eliminada exitosamente", true);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error al eliminar estación ID {}", idEstacion, e);
            return OperacionResultadoDTO.error("Error al eliminar la estación");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BienesParaEstacionResponseDTO obtenerBienesParaFormulario(Long idEstacion) {
        log.info("Obteniendo bienes para formulario. Estación: {}", idEstacion != null ? idEstacion : "NUEVA");

        List<BienAsignadoDTO> bienesLibres = bienEstacionRepository.findBienesLibres().stream()
                .map(this::toBienAsignadoDTO)
                .toList();

        List<BienAsignadoDTO> bienesEnOtras = bienEstacionRepository.findBienesAsignadosExcluyendoEstacion(idEstacion)
                .stream()
                .map(this::toBienAsignadoDTOConEstacion)
                .toList();

        List<BienAsignadoDTO> bienesEnActual = idEstacion != null
                ? bienEstacionRepository.findByEstacionIdAndFechaDesasignacionIsNull(idEstacion).stream()
                        .map(this::toBienAsignadoDTOConEstacion)
                        .toList()
                : List.of();

        return BienesParaEstacionResponseDTO.of(bienesLibres, bienesEnOtras, bienesEnActual);
    }

    // ==================== MÉTODOS HELPER PRIVADOS ====================

    private String generarCodigoEstacion(String codigoInput, Ambiente ambiente) {
        if (codigoInput == null || codigoInput.trim().isEmpty()) {
            return null;
        }

        String nombreAbreviado = abreviarNombreAmbiente(ambiente.getNombre());
        return codigoInput.trim() + " - (" + nombreAbreviado + ")";
    }

    private String abreviarNombreAmbiente(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "";
        }

        String resultado = nombreCompleto.trim()
                .replaceAll("(?i)\\bLaboratorio\\b", "Lab.")
                .replaceAll("(?i)\\bEspecializado\\b", "")
                .replaceAll("(?i)\\bOficina\\b", "Of.")
                .replaceAll("(?i)\\bDepartamento\\b", "Dpto.")
                .replaceAll("(?i)\\bAdministración\\b", "Admin.")
                .replaceAll("(?i)\\bInformática\\b", "Inf.")
                .replaceAll("(?i)\\bIngeniería\\b", "Ing.")
                .replaceAll("(?i)\\bSistemas\\b", "Sist.")
                .replaceAll("(?i)\\bContabilidad\\b", "Cont.")
                .replaceAll("(?i)\\bRecursos Humanos\\b", "RRHH")
                .replaceAll("(?i)\\bComunicaciones\\b", "Com.")
                .replaceAll("(?i)\\bAcadémic[oa]\\b", "Acad.")
                .replaceAll("\\s{2,}", " ")
                .trim();

        String[] palabras = resultado.split("\\s+");
        if (palabras.length > 5) {
            resultado = String.join(" ", java.util.Arrays.copyOf(palabras, 5));
        }

        return resultado;
    }

    private Integer calcularCapacidadBienes(TipoEstructuraEntity tipoEstructura) {
        if (tipoEstructura.getConfiguracionCampos() != null) {
            Object capacidadConfig = tipoEstructura.getConfiguracionCampos().get("capacidadBienes");
            if (capacidadConfig instanceof Number) {
                return ((Number) capacidadConfig).intValue();
            }
        }

        String nombreTipo = tipoEstructura.getNombre().toLowerCase();

        if (nombreTipo.contains("escritorio") || nombreTipo.contains("desk")) return 5;
        if (nombreTipo.contains("mesa") || nombreTipo.contains("table")) return 8;
        if (nombreTipo.contains("rack") || nombreTipo.contains("estante")) return 20;
        if (nombreTipo.contains("armario") || nombreTipo.contains("cabinet")) return 15;

        return 10;
    }

    private long contarBienesAsignadosActivos(Long idEstacion) {
        return bienEstacionRepository.countByEstacionIdAndFechaDesasignacionIsNull(idEstacion);
    }

    private Estacion buscarEstacionPorId(Long idEstacion) {
        return estacionRepository.findById(idEstacion)
                .orElseThrow(() -> new RuntimeException("Estación no encontrada con ID: " + idEstacion));
    }

    private EstacionDTO obtenerDTOConEstadisticas(Estacion estacion) {
        EstacionDTO dto = estacionMapper.toDTO(estacion);
        agregarEstadisticas(dto);
        return dto;
    }

    private List<EstacionDTO> obtenerDTOsConEstadisticas(List<Estacion> estaciones) {
        List<EstacionDTO> dtos = estacionMapper.toDTOList(estaciones);
        dtos.forEach(this::agregarEstadisticas);
        return dtos;
    }

    private void agregarEstadisticas(EstacionDTO dto) {
        if (dto == null || dto.getIdEstacion() == null) return;

        long bienesAsignados = contarBienesAsignadosActivos(dto.getIdEstacion());
        dto.setCantidadBienesAsignados(bienesAsignados);

        if (dto.getCapacidadBienes() != null) {
            dto.setCapacidadDisponible((int) (dto.getCapacidadBienes() - bienesAsignados));
        }
    }

    private BienAsignadoDTO toBienAsignadoDTO(Bien bien) {
        return new BienAsignadoDTO(
                bien.getId(),
                bien.getNombreBien(),
                bien.getCaf(),
                bien.getNumeroSerie(),
                bien.getCategoria() != null ? bien.getCategoria().getId() : null,
                bien.getCategoria() != null ? bien.getCategoria().getNombreCategoria() : null,
                bien.getEstadoFisico(),
                bien.getEstadoOperacional(),
                extraerAtributo(bien, "Marca"),
                extraerAtributo(bien, "Modelo"),
                bien.getResponsableActual() != null
                        ? bien.getResponsableActual().getNombre() + " " + bien.getResponsableActual().getApellido()
                        : null,
                null,
                null
        );
    }

    private BienAsignadoDTO toBienAsignadoDTOConEstacion(BienEstacion be) {
        Bien bien = be.getBien();
        Estacion estacion = be.getEstacion();

        return new BienAsignadoDTO(
                bien.getId(),
                bien.getNombreBien(),
                bien.getCaf(),
                bien.getNumeroSerie(),
                bien.getCategoria() != null ? bien.getCategoria().getId() : null,
                bien.getCategoria() != null ? bien.getCategoria().getNombreCategoria() : null,
                bien.getEstadoFisico(),
                bien.getEstadoOperacional(),
                extraerAtributo(bien, "Marca"),
                extraerAtributo(bien, "Modelo"),
                bien.getResponsableActual() != null
                        ? bien.getResponsableActual().getNombre() + " " + bien.getResponsableActual().getApellido()
                        : null,
                estacion.getIdEstacion(),
                estacion.getNombre()
        );
    }

    private String extraerAtributo(Bien bien, String nombreAtributo) {
        if (bien.getAtributos() == null) return null;

        return bien.getAtributos().stream()
                .filter(attr -> attr.getTipoAtributo() != null
                        && nombreAtributo.equalsIgnoreCase(attr.getTipoAtributo().getNombreAtributo()))
                .map(attr -> attr.getValor())
                .findFirst()
                .orElse(null);
    }
}