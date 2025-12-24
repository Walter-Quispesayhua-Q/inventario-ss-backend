package com.upeu.gestioninventario.estructuras.service.impl;

import com.upeu.gestioninventario.estructuras.dto.ComponenteResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.ConfirmacionOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.ResultadoOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteInputDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioConPisosResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioInputDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.*;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoConAmbientesResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoInputDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoResumenDTO;
import com.upeu.gestioninventario.estructuras.model.BienEstacion;
import com.upeu.gestioninventario.estructuras.repository.BienEstacionRepository;
import com.upeu.gestioninventario.estructuras.service.*;
import com.upeu.gestioninventario.inventario.model.Bien;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EstructuraCoordinadorServiceImpl implements IEstructuraCoordinadorService {

    private final IEdificioService edificioService;
    private final IPisoService pisoService;
    private final IAmbienteService ambienteService;
    private final IEstacionService estacionService;
    private final IComponenteService componenteService;
    private final BienEstacionRepository bienEstacionRepository;

    @Override
    public EdificioConPisosResponseDTO crearEdificioConPisos(EdificioInputDTO input) {
        log.info("Creando edificio '{}' con {} piso(s)",
                input.getNombre(), input.getPisos() != null ? input.getPisos().size() : 0);

        var resultadoEdificio = edificioService.crearEdificio(input);
        if (!resultadoEdificio.getExito() || resultadoEdificio.getData() == null) {
            throw new RuntimeException("Error al crear edificio: " + resultadoEdificio.getMensaje());
        }
        EdificioDTO edificioCreado = resultadoEdificio.getData();
        log.info("Edificio creado con ID: {}", edificioCreado.getIdEdificio());

        List<PisoResumenDTO> pisosCreados = new ArrayList<>();

        if (input.getPisos() != null && !input.getPisos().isEmpty()) {
            for (PisoInputDTO inputPiso : input.getPisos()) {
                inputPiso.setIdEdificio(edificioCreado.getIdEdificio());
                var resultadoPiso = pisoService.crearPiso(inputPiso);
                if (!resultadoPiso.getExito() || resultadoPiso.getData() == null) {
                    throw new RuntimeException("Error al crear piso: " + resultadoPiso.getMensaje());
                }
                PisoDTO pisoCreado = resultadoPiso.getData();
                pisosCreados.add(toPisoResumen(pisoCreado));
                log.info("Piso creado: {} (ID: {})", pisoCreado.getNombre(), pisoCreado.getIdPiso());
            }
        }

        log.info("Edificio con pisos creado. Total pisos: {}", pisosCreados.size());
        return buildEdificioConPisosResponse(edificioCreado, pisosCreados);
    }

    @Override
    @Transactional(readOnly = true)
    public EdificioConPisosResponseDTO obtenerEdificioConPisos(Long idEdificio) {
        log.debug("Obteniendo edificio {} con sus pisos", idEdificio);

        EdificioDTO edificio = edificioService.obtenerEdificioPorId(idEdificio);
        List<PisoDTO> pisos = pisoService.listarPisosPorEdificio(idEdificio);
        List<PisoResumenDTO> pisosResumen = pisos.stream()
                .map(piso -> toPisoResumenConCantidad(piso, ambienteService.contarAmbientesPorPiso(piso.getIdPiso())))
                .toList();

        return buildEdificioConPisosResponse(edificio, pisosResumen);
    }

    @Override
    public EdificioConPisosResponseDTO actualizarEdificioConPisos(Long idEdificio, EdificioInputDTO input) {
        log.info("Actualizando edificio {} con {} piso(s)",
                idEdificio, input.getPisos() != null ? input.getPisos().size() : 0);

        var resultadoEdificio = edificioService.actualizarEdificio(idEdificio, input);
        if (!resultadoEdificio.getExito() || resultadoEdificio.getData() == null) {
            throw new RuntimeException("Error al actualizar edificio: " + resultadoEdificio.getMensaje());
        }
        EdificioDTO edificioActualizado = resultadoEdificio.getData();

        List<PisoResumenDTO> pisosResultado = new ArrayList<>();

        if (input.getPisos() != null) {
            List<PisoDTO> pisosExistentes = pisoService.listarPisosPorEdificio(idEdificio);
            
            for (PisoInputDTO inputPiso : input.getPisos()) {
                inputPiso.setIdEdificio(idEdificio);
                
                PisoDTO pisoExistente = null;

                // 1. Intentar buscar por ID (prioridad)
                if (inputPiso.getIdPiso() != null) {
                    pisoExistente = pisosExistentes.stream()
                            .filter(p -> p.getIdPiso().equals(inputPiso.getIdPiso()))
                            .findFirst()
                            .orElse(null);
                }

                // 2. Si no se encontró por ID, intentar por CÓDIGO (fallback)
                if (pisoExistente == null) {
                    pisoExistente = pisosExistentes.stream()
                            .filter(p -> p.getCodigo().equals(inputPiso.getCodigo()))
                            .findFirst()
                            .orElse(null);
                }

                if (pisoExistente != null) {
                    var resultadoPiso = pisoService.actualizarPiso(pisoExistente.getIdPiso(), inputPiso);
                    if (!resultadoPiso.getExito() || resultadoPiso.getData() == null) {
                        throw new RuntimeException("Error al actualizar piso: " + resultadoPiso.getMensaje());
                    }
                    PisoDTO actualizado = resultadoPiso.getData();
                    pisosResultado.add(toPisoResumen(actualizado));
                } else {
                    var resultadoPiso = pisoService.crearPiso(inputPiso);
                    if (!resultadoPiso.getExito() || resultadoPiso.getData() == null) {
                        throw new RuntimeException("Error al crear piso: " + resultadoPiso.getMensaje());
                    }
                    PisoDTO creado = resultadoPiso.getData();
                    pisosResultado.add(toPisoResumen(creado));
                }
            }
        } else {
            List<PisoDTO> pisos = pisoService.listarPisosPorEdificio(idEdificio);
            pisosResultado = pisos.stream().map(this::toPisoResumen).toList();
        }

        log.info("Edificio actualizado. Total pisos: {}", pisosResultado.size());
        return buildEdificioConPisosResponse(edificioActualizado, pisosResultado);
    }

    @Override
    public ResultadoOperacionDTO eliminarEdificio(Long idEdificio, ConfirmacionOperacionDTO confirmacion) {
        EdificioDTO edificio = edificioService.obtenerEdificioPorId(idEdificio);
        List<PisoDTO> pisos = pisoService.listarPisosPorEdificio(idEdificio);

        if (!pisos.isEmpty() && (confirmacion == null || !confirmacion.confirmado())) {
            String codigo = UUID.randomUUID().toString().substring(0, 8);
            String mensaje = String.format(
                    "El edificio '%s' tiene %d piso(s). ¿Confirma la eliminación completa?",
                    edificio.getNombre(), pisos.size());
            return ResultadoOperacionDTO.requiereConfirmacion(codigo, mensaje);
        }

        for (PisoDTO piso : pisos) {
            List<AmbienteDTO> ambientes = ambienteService.listarAmbientesPorPiso(piso.getIdPiso());
            for (AmbienteDTO ambiente : ambientes) {
                var resultadoAmb = ambienteService.eliminarAmbiente(ambiente.getIdAmbiente());
                if (!resultadoAmb.getExito()) {
                    log.warn("Error eliminando ambiente {}: {}", ambiente.getIdAmbiente(), resultadoAmb.getMensaje());
                }
            }
            var resultadoPiso = pisoService.eliminarPiso(piso.getIdPiso());
            if (!resultadoPiso.getExito()) {
                log.warn("Error eliminando piso {}: {}", piso.getIdPiso(), resultadoPiso.getMensaje());
            }
        }
        var resultadoEdificio = edificioService.eliminarEdificio(idEdificio);
        if (!resultadoEdificio.getExito()) {
            throw new RuntimeException("Error al eliminar edificio: " + resultadoEdificio.getMensaje());
        }

        log.info("Edificio {} eliminado con {} piso(s)", idEdificio, pisos.size());
        return ResultadoOperacionDTO.exitoso(
                String.format("Edificio '%s' eliminado correctamente", edificio.getNombre()));
    }

    @Override
    public ResultadoOperacionDTO eliminarPiso(Long idEdificio, Long idPiso, ConfirmacionOperacionDTO confirmacion) {
        PisoDTO piso = pisoService.obtenerPisoPorId(idPiso);
        List<AmbienteDTO> ambientes = ambienteService.listarAmbientesPorPiso(idPiso);

        if (!ambientes.isEmpty() && (confirmacion == null || !confirmacion.confirmado())) {
            String codigo = UUID.randomUUID().toString().substring(0, 8);
            String mensaje = String.format(
                    "El piso '%s' tiene %d ambiente(s). ¿Confirma la eliminación?",
                    piso.getNombre(), ambientes.size());
            return ResultadoOperacionDTO.requiereConfirmacion(codigo, mensaje);
        }

        for (AmbienteDTO ambiente : ambientes) {
            var resultadoAmb = ambienteService.eliminarAmbiente(ambiente.getIdAmbiente());
            if (!resultadoAmb.getExito()) {
                log.warn("Error eliminando ambiente {}: {}", ambiente.getIdAmbiente(), resultadoAmb.getMensaje());
            }
        }
        var resultadoPiso = pisoService.eliminarPiso(idPiso);
        if (!resultadoPiso.getExito()) {
            throw new RuntimeException("Error al eliminar piso: " + resultadoPiso.getMensaje());
        }

        log.info("Piso {} eliminado con {} ambiente(s)", idPiso, ambientes.size());
        return ResultadoOperacionDTO.exitoso(
                String.format("Piso '%s' eliminado correctamente", piso.getNombre()));
    }

    @Override
    public PisoResumenDTO agregarPisoAEdificio(Long idEdificio, PisoInputDTO input) {
        input.setIdEdificio(idEdificio);
        var resultadoPiso = pisoService.crearPiso(input);
        if (!resultadoPiso.getExito() || resultadoPiso.getData() == null) {
            throw new RuntimeException("Error al agregar piso: " + resultadoPiso.getMensaje());
        }
        PisoDTO pisoCreado = resultadoPiso.getData();
        log.info("Piso agregado al edificio {}: {} (ID: {})", 
                idEdificio, pisoCreado.getNombre(), pisoCreado.getIdPiso());
        return toPisoResumen(pisoCreado);
    }

    // ===== PISO + AMBIENTES =====
    @Override
    @Transactional(readOnly = true)
    public PisoConAmbientesResponseDTO obtenerPisoConAmbientes(Long idPiso) {
        log.debug("Obteniendo piso {} con sus ambientes", idPiso);

        PisoDTO piso = pisoService.obtenerPisoPorId(idPiso);
        List<AmbienteDTO> ambientes = ambienteService.listarAmbientesPorPiso(idPiso);
        List<AmbienteResumenDTO> ambientesResumen = ambientes.stream().map(this::toAmbienteResumen).toList();

        return buildPisoConAmbientesResponse(piso, ambientesResumen);
    }

    @Override
    public PisoConAmbientesResponseDTO actualizarPisoConAmbientes(Long idPiso, PisoInputDTO input) {
        log.info("Actualizando piso {} con {} ambiente(s)",
                idPiso, input.getAmbientes() != null ? input.getAmbientes().size() : 0);

        var resultadoPiso = pisoService.actualizarPiso(idPiso, input);
        if (!resultadoPiso.getExito() || resultadoPiso.getData() == null) {
            throw new RuntimeException("Error al actualizar piso: " + resultadoPiso.getMensaje());
        }
        PisoDTO pisoActualizado = resultadoPiso.getData();

        List<AmbienteResumenDTO> ambientesResultado = new ArrayList<>();

        if (input.getAmbientes() != null) {
            List<AmbienteDTO> ambientesExistentes = ambienteService.listarAmbientesPorPiso(idPiso);

            for (AmbienteInputDTO inputAmbiente : input.getAmbientes()) {
                inputAmbiente.setIdPiso(idPiso);

                AmbienteDTO ambienteExistente = ambientesExistentes.stream()
                        .filter(a -> a.getCodigo().equals(inputAmbiente.getCodigo()))
                        .findFirst()
                        .orElse(null);

                if (ambienteExistente != null) {
                    var resultadoAmb = ambienteService.actualizarAmbiente(ambienteExistente.getIdAmbiente(), inputAmbiente);
                    if (!resultadoAmb.getExito() || resultadoAmb.getData() == null) {
                        throw new RuntimeException("Error al actualizar ambiente: " + resultadoAmb.getMensaje());
                    }
                    AmbienteDTO actualizado = resultadoAmb.getData();
                    ambientesResultado.add(toAmbienteResumen(actualizado));
                } else {
                    var resultadoAmb = ambienteService.crearAmbiente(inputAmbiente);
                    if (!resultadoAmb.getExito() || resultadoAmb.getData() == null) {
                        throw new RuntimeException("Error al crear ambiente: " + resultadoAmb.getMensaje());
                    }
                    AmbienteDTO creado = resultadoAmb.getData();
                    ambientesResultado.add(toAmbienteResumen(creado));
                }
            }
        } else {
            List<AmbienteDTO> ambientes = ambienteService.listarAmbientesPorPiso(idPiso);
            ambientesResultado = ambientes.stream().map(this::toAmbienteResumen).toList();
        }

        log.info("Piso actualizado. Total ambientes: {}", ambientesResultado.size());
        return buildPisoConAmbientesResponse(pisoActualizado, ambientesResultado);
    }

    @Override
    public AmbienteResumenDTO agregarAmbienteAPiso(Long idPiso, AmbienteInputDTO input) {
        input.setIdPiso(idPiso);
        var resultadoAmb = ambienteService.crearAmbiente(input);
        if (!resultadoAmb.getExito() || resultadoAmb.getData() == null) {
            throw new RuntimeException("Error al agregar ambiente: " + resultadoAmb.getMensaje());
        }
        AmbienteDTO ambienteCreado = resultadoAmb.getData();
        log.info("Ambiente agregado al piso {}: {} (ID: {})",
                idPiso, ambienteCreado.getNombre(), ambienteCreado.getIdAmbiente());
        return toAmbienteResumen(ambienteCreado);
    }

    // ===== AMBIENTE (CRUD standalone) =====
    @Override
    public AmbienteResumenDTO actualizarAmbiente(Long idAmbiente, AmbienteInputDTO input, ConfirmacionOperacionDTO confirmacion) {
        List<EstacionDTO> estaciones = estacionService.listarEstacionesPorAmbiente(idAmbiente);

        if (!estaciones.isEmpty() && (confirmacion == null || !confirmacion.confirmado())) {
            log.info("Ambiente {} tiene {} estaciones, requiere confirmación para actualizar", idAmbiente, estaciones.size());
        }

        var resultadoAmb = ambienteService.actualizarAmbiente(idAmbiente, input);
        if (!resultadoAmb.getExito() || resultadoAmb.getData() == null) {
            throw new RuntimeException("Error al actualizar ambiente: " + resultadoAmb.getMensaje());
        }
        AmbienteDTO ambienteActualizado = resultadoAmb.getData();
        log.info("Ambiente {} actualizado", idAmbiente);
        return toAmbienteResumen(ambienteActualizado);
    }

    @Override
    public ResultadoOperacionDTO eliminarAmbiente(Long idAmbiente, ConfirmacionOperacionDTO confirmacion) {
        AmbienteDTO ambiente = ambienteService.obtenerAmbientePorId(idAmbiente);
        List<EstacionDTO> estaciones = estacionService.listarEstacionesPorAmbiente(idAmbiente);

        if (!estaciones.isEmpty() && (confirmacion == null || !confirmacion.confirmado())) {
            String codigo = UUID.randomUUID().toString().substring(0, 8);
            String mensaje = String.format(
                    "El ambiente '%s' tiene %d estación(es). ¿Confirma la eliminación?",
                    ambiente.getNombre(), estaciones.size());
            return ResultadoOperacionDTO.requiereConfirmacion(codigo, mensaje);
        }

        for (EstacionDTO estacion : estaciones) {
            var resultadoEst = estacionService.eliminarEstacion(estacion.getIdEstacion());
            if (!resultadoEst.getExito()) {
                log.warn("Error eliminando estación {}: {}", estacion.getIdEstacion(), resultadoEst.getMensaje());
            }
        }
        var resultadoAmb = ambienteService.eliminarAmbiente(idAmbiente);
        if (!resultadoAmb.getExito()) {
            throw new RuntimeException("Error al eliminar ambiente: " + resultadoAmb.getMensaje());
        }

        log.info("Ambiente {} eliminado con {} estación(es)", idAmbiente, estaciones.size());
        return ResultadoOperacionDTO.exitoso(
                String.format("Ambiente '%s' eliminado correctamente", ambiente.getNombre()));
    }

    // ===== ESTACIÓN (CRUD standalone) =====

    @Override
    public EstacionResumenDTO agregarEstacionAAmbiente(Long idAmbiente, EstacionInputDTO input) {
        log.info("Agregando estación '{}' al ambiente {}", input.getNombre(), idAmbiente);
        
        if (!ambienteService.existeAmbiente(idAmbiente)) {
            throw new RuntimeException("Ambiente no encontrado: " + idAmbiente);
        }
        
        input.setIdAmbiente(idAmbiente);
        var resultadoEst = estacionService.crearEstacion(input);
        if (!resultadoEst.getExito() || resultadoEst.getData() == null) {
            throw new RuntimeException("Error al agregar estación: " + resultadoEst.getMensaje());
        }
        EstacionDTO estacionCreada = resultadoEst.getData();
        log.info("Estación creada con ID: {}", estacionCreada.getIdEstacion());
        return toEstacionResumen(estacionCreada);
    }

    @Override
    @Transactional(readOnly = true)
    public EstacionConBienesResponseDTO obtenerEstacion(Long idEstacion) {
        log.debug("Obteniendo estación {} con componentes y bienes individuales", idEstacion);
        
        EstacionDTO estacion = estacionService.obtenerEstacionPorId(idEstacion);
        List<ComponenteEstacionDTO> componentes = componenteService.listarComponentesPorEstacion(idEstacion);
        
        // Bienes individuales: asignados a la estación pero SIN componente
        List<BienEstacion> bienesIndividuales = bienEstacionRepository.findBienesIndividualesPorEstacion(idEstacion);
        
        return buildEstacionConBienesResponse(estacion, componentes, bienesIndividuales);
    }

    @Override
    public EstacionResumenDTO actualizarEstacion(Long idEstacion, EstacionInputDTO input, ConfirmacionOperacionDTO confirmacion) {
        EstacionDTO estacion = estacionService.obtenerEstacionPorId(idEstacion);
        long bienesAsignados = estacion.getCantidadBienesAsignados() != null ? estacion.getCantidadBienesAsignados() : 0;

        if (bienesAsignados > 0 && (confirmacion == null || !confirmacion.confirmado())) {
            log.info("Estación {} tiene {} bienes, requiere confirmación", idEstacion, bienesAsignados);
        }

        var resultadoEst = estacionService.actualizarEstacion(idEstacion, input);
        if (!resultadoEst.getExito() || resultadoEst.getData() == null) {
            throw new RuntimeException("Error al actualizar estación: " + resultadoEst.getMensaje());
        }
        EstacionDTO estacionActualizada = resultadoEst.getData();
        log.info("Estación {} actualizada", idEstacion);
        return toEstacionResumen(estacionActualizada);
    }

    @Override
    public ResultadoOperacionDTO eliminarEstacion(Long idEstacion, ConfirmacionOperacionDTO confirmacion) {
        EstacionDTO estacion = estacionService.obtenerEstacionPorId(idEstacion);
        long bienesAsignados = estacion.getCantidadBienesAsignados() != null ? estacion.getCantidadBienesAsignados() : 0;

        if (bienesAsignados > 0 && (confirmacion == null || !confirmacion.confirmado())) {
            String codigo = UUID.randomUUID().toString().substring(0, 8);
            String mensaje = String.format(
                    "La estación '%s' tiene %d bien(es) asignados. ¿Confirma la eliminación?",
                    estacion.getNombre(), bienesAsignados);
            return ResultadoOperacionDTO.requiereConfirmacion(codigo, mensaje);
        }

        var resultadoEst = estacionService.eliminarEstacion(idEstacion);
        if (!resultadoEst.getExito()) {
            throw new RuntimeException("Error al eliminar estación: " + resultadoEst.getMensaje());
        }
        log.info("Estación {} eliminada", idEstacion);
        return ResultadoOperacionDTO.exitoso(
                String.format("Estación '%s' eliminada correctamente", estacion.getNombre()));
    }

    // ===== HELPERS =====

    private PisoResumenDTO toPisoResumen(PisoDTO piso) {
        return new PisoResumenDTO(
                piso.getIdPiso(),
                piso.getNombre(),
                piso.getCodigo(),
                piso.getNumeroPiso(),
                piso.getTipoEstructura(),
                piso.getCantidadAmbientes()
        );
    }

    private PisoResumenDTO toPisoResumenConCantidad(PisoDTO piso, long cantidadAmbientes) {
        return new PisoResumenDTO(
                piso.getIdPiso(),
                piso.getNombre(),
                piso.getCodigo(),
                piso.getNumeroPiso(),
                piso.getTipoEstructura(),
                cantidadAmbientes
        );
    }

    private AmbienteResumenDTO toAmbienteResumen(AmbienteDTO ambiente) {
        return new AmbienteResumenDTO(
                ambiente.getIdAmbiente(),
                ambiente.getNombre(),
                ambiente.getCodigo(),
                ambiente.getCapacidadPersonas(),
                ambiente.getTipoEstructura(),
                ambiente.getNombreResponsable(),
                ambiente.getCantidadEstaciones()
        );
    }

    private EstacionResumenDTO toEstacionResumen(EstacionDTO estacion) {
        int capacidad = estacion.getCapacidadBienes() != null ? estacion.getCapacidadBienes() : 0;
        long asignados = estacion.getCantidadBienesAsignados() != null ? estacion.getCantidadBienesAsignados() : 0;
        int disponible = capacidad - (int) asignados;
        
        return new EstacionResumenDTO(
                estacion.getIdEstacion(),
                estacion.getNombre(),
                estacion.getCodigo(),
                estacion.getCapacidadBienes(),
                asignados,
                disponible,
                estacion.getTipoEstructura(),
                estacion.getObservaciones()
        );
    }

    private EdificioConPisosResponseDTO buildEdificioConPisosResponse(
            EdificioDTO edificio, List<PisoResumenDTO> pisos) {
        return new EdificioConPisosResponseDTO(
                edificio.getIdEdificio(),
                edificio.getNombre(),
                edificio.getCodigo(),
                edificio.getTipoEstructura(),
                edificio.getDireccion(),
                edificio.getNumeroPisos(),
                edificio.getIdResponsableMantenimiento(),
                edificio.getNombreResponsableMantenimiento(),
                edificio.getObservaciones(),
                pisos,
                (long) pisos.size()
        );
    }

    private PisoConAmbientesResponseDTO buildPisoConAmbientesResponse(
            PisoDTO piso, List<AmbienteResumenDTO> ambientes) {
        return new PisoConAmbientesResponseDTO(
                piso.getIdPiso(),
                piso.getNombre(),
                piso.getCodigo(),
                piso.getNumeroPiso(),
                piso.getTipoEstructura(),
                piso.getIdEdificio(),
                piso.getNombreEdificio(),
                piso.getCodigoEdificio(),
                piso.getIdResponsableMantenimiento(),
                piso.getNombreResponsableMantenimiento(),
                piso.getObservaciones(),
                ambientes,
                (long) ambientes.size()
        );
    }

    private EstacionConBienesResponseDTO buildEstacionConBienesResponse(
            EstacionDTO estacion, 
            List<ComponenteEstacionDTO> componentes,
            List<BienEstacion> bienesIndividuales) {
        
        List<ComponenteResumenDTO> componentesResumen = componentes.stream()
                .map(this::toComponenteResumen)
                .toList();

        // Bienes individuales (sin componente) como resumen
        List<BienAsignadoDTO> bienesIndividualesResumen = bienesIndividuales.stream()
                .map(this::toBienAsignadoFromEntity)
                .toList();

        int totalBienesEnComponentes = componentes.stream()
                .mapToInt(ComponenteEstacionDTO::totalBienes)
                .sum();
        
        int totalBienes = totalBienesEnComponentes + bienesIndividualesResumen.size();
        int capacidad = estacion.getCapacidadBienes() != null ? estacion.getCapacidadBienes() : 0;
        int disponible = capacidad - totalBienes;
        
        // Priorizar responsable de la estación, si no existe, usar el del ambiente
        String responsableNombre = estacion.getNombreResponsable() != null
                ? estacion.getNombreResponsable()
                : (estacion.getAmbiente() != null ? estacion.getAmbiente().getNombreResponsable() : null);

        return new EstacionConBienesResponseDTO(
                estacion.getIdEstacion(),
                estacion.getNombre(),
                estacion.getCodigo(),
                estacion.getIdAmbiente(),
                estacion.getNombreAmbiente(),
                estacion.getIdPiso(),
                estacion.getNombrePiso(),
                estacion.getNumeroPiso(),
                estacion.getIdEdificio(),
                estacion.getNombreEdificio(),
                estacion.getTipoEstructura(),
                estacion.getCapacidadBienes(),
                disponible,
                totalBienes,
                responsableNombre,
                componentesResumen,
                bienesIndividualesResumen,
                componentesResumen.size(),
                totalBienes,
                bienesIndividualesResumen.size(),
                estacion.getPropiedadesAdicionales(),
                estacion.getObservaciones()
        );
    }

    private ComponenteResumenDTO toComponenteResumen(ComponenteEstacionDTO componente) {
        List<BienAsignadoDTO> bienes = componente.getBienes() != null
                ? componente.getBienes().stream().map(this::toBienAsignado).toList()
                : List.of();

        return new ComponenteResumenDTO(
                componente.getId(),
                componente.getNombre(),
                componente.getTipo(),
                componente.getCategoriaBase(),
                componente.getOrden(),
                bienes,
                bienes.size()
        );
    }

    private BienAsignadoDTO toBienAsignado(BienEstacionDTO bien) {
        return new BienAsignadoDTO(
                bien.getIdBien(),
                bien.getNombreBien(),
                bien.getCodigoBien(),
                bien.getNumeroSerieBien(),
                null,
                bien.getCategoriaDetectada(),
                bien.getEstadoFisico(),
                bien.getEstadoOperacional(),
                bien.getMarca(),
                bien.getModelo(),
                bien.getResponsableActualNombre(),
                null,
                null
        );
    }

    private BienAsignadoDTO toBienAsignadoFromEntity(BienEstacion be) {
        var bien = be.getBien();
        
        String marca = extraerAtributo(bien, "Marca");
        String modelo = extraerAtributo(bien, "Modelo");
        String responsableNombre = bien.getResponsableActual() != null
                ? bien.getResponsableActual().getNombre() + " " + bien.getResponsableActual().getApellido()
                : null;
        
        return new BienAsignadoDTO(
                bien.getId(),
                bien.getNombreBien(),
                bien.getCaf(),
                bien.getNumeroSerie(),
                bien.getCategoria() != null ? bien.getCategoria().getId() : null,
                bien.getCategoria() != null ? bien.getCategoria().getNombreCategoria() : null,
                bien.getEstadoFisico(),
                bien.getEstadoOperacional(),
                marca,
                modelo,
                responsableNombre,
                null,
                null
        );
    }
    
    private String extraerAtributo(Bien bien, String nombreAtributo) {
        if (bien.getAtributos() == null) {
            return null;
        }
        return bien.getAtributos().stream()
                .filter(attr -> attr.getTipoAtributo() != null 
                        && nombreAtributo.equalsIgnoreCase(attr.getTipoAtributo().getNombreAtributo()))
                .map(attr -> attr.getValor())
                .findFirst()
                .orElse(null);
    }
}