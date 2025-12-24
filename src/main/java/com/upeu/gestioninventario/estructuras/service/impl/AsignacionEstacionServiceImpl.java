package com.upeu.gestioninventario.estructuras.service.impl;

import com.upeu.gestioninventario.estructuras.dto.BienYaAsignadoDTO;
import com.upeu.gestioninventario.estructuras.dto.asignacion.*;
import com.upeu.gestioninventario.estructuras.model.*;
import com.upeu.gestioninventario.estructuras.repository.*;
import com.upeu.gestioninventario.estructuras.service.IAsignacionEstacionService;
import com.upeu.gestioninventario.inventario.model.Bien;
import com.upeu.gestioninventario.inventario.repository.BienRepository;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import com.upeu.gestioninventario.ubicaciones.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AsignacionEstacionServiceImpl implements IAsignacionEstacionService {

    private final BienEstacionRepository bienEstacionRepository;
    private final EstacionRepository estacionRepository;
    private final AmbienteRepository ambienteRepository;
    private final PisoRepository pisoRepository;
    private final EdificioRepository edificioRepository;
    private final BienRepository bienRepository;
    private final UbicacionRepository ubicacionRepository;

    @Override
    public ResultadoAsignacionDTO asignarBienAEstacion(
            Long idBien,
            Long idEstacion,
            String posicionRelativa,
            Long idUsuario,
            boolean forzarReasignacion) {

        log.info("Asignando bien {} a estacion {} (forzar: {})", idBien, idEstacion, forzarReasignacion);

        Bien bien = buscarBienPorId(idBien);
        Estacion estacion = buscarEstacionPorId(idEstacion);

        Optional<BienEstacion> asignacionExistente = bienEstacionRepository
                .findByBienIdAndFechaDesasignacionIsNull(idBien);

        if (asignacionExistente.isPresent()) {
            BienEstacion asignacionActual = asignacionExistente.get();
            log.info("Bien {} esta asignado a estacion {}", idBien, asignacionActual.getEstacion().getIdEstacion());

            if (asignacionActual.getEstacion().getIdEstacion().equals(idEstacion)) {
                return ResultadoAsignacionDTO.builder()
                        .exito(false)
                        .requiereConfirmacion(false)
                        .mensaje("El bien ya esta asignado a esta estacion")
                        .build();
            }

            if (!forzarReasignacion) {
                String nombreEstacionActual = asignacionActual.getEstacion().getNombre();
                return ResultadoAsignacionDTO.builder()
                        .exito(false)
                        .requiereConfirmacion(true)
                        .mensaje("El bien esta en la estacion '" + nombreEstacionActual + "'. Desea reasignarlo?")
                        .idEstacionActual(asignacionActual.getEstacion().getIdEstacion())
                        .nombreEstacionActual(nombreEstacionActual)
                        .build();
            }

            log.info("Reasignacion forzada - Bien {} desde estacion {} a estacion {}",
                    idBien, asignacionActual.getEstacion().getIdEstacion(), idEstacion);
            desasignarBienDeEstacion(idBien, idUsuario);
        }

        validarCapacidadEstacion(estacion);

        BienEstacion nuevaAsignacion = BienEstacion.builder()
                .bien(bien)
                .estacion(estacion)
                .posicionRelativa(posicionRelativa)
                .fechaAsignacion(LocalDateTime.now())
                .idUsuarioAsignacion(idUsuario)
                .build();

        BienEstacion asignacionGuardada = bienEstacionRepository.save(nuevaAsignacion);
        actualizarUbicacionDesdeEstacion(bien, estacion);

        log.info("Bien {} asignado a estacion {}", idBien, idEstacion);

        return ResultadoAsignacionDTO.builder()
                .exito(true)
                .requiereConfirmacion(false)
                .mensaje("Bien asignado a la estacion '" + estacion.getNombre() + "'")
                .idAsignacion(asignacionGuardada.getId())
                .build();
    }

    @Override
    public ResultadoAsignacionMasivaDTO asignarBienesMasivos(
            List<Long> idsBienes,
            Long idEstacion,
            Long idUsuario,
            boolean forzarReasignacion) {

        log.info("Asignacion masiva de {} bienes a estacion {}", idsBienes.size(), idEstacion);

        List<Long> bienesLibres = new ArrayList<>();
        List<BienYaAsignadoDTO> bienesYaAsignados = new ArrayList<>();
        List<Long> bienesAsignados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (Long idBien : idsBienes) {
            Optional<BienEstacion> asignacionExistente = bienEstacionRepository
                    .findByBienIdAndFechaDesasignacionIsNull(idBien);

            if (asignacionExistente.isPresent()) {
                BienEstacion asignacion = asignacionExistente.get();
                if (!asignacion.getEstacion().getIdEstacion().equals(idEstacion)) {
                    bienesYaAsignados.add(BienYaAsignadoDTO.builder()
                            .idBien(idBien)
                            .idEstacionActual(asignacion.getEstacion().getIdEstacion())
                            .nombreEstacionActual(asignacion.getEstacion().getNombre())
                            .build());
                }
            } else {
                bienesLibres.add(idBien);
            }
        }

        for (Long idBien : bienesLibres) {
            try {
                ResultadoAsignacionDTO resultado = asignarBienAEstacion(idBien, idEstacion, null, idUsuario, false);
                if (resultado.isExito()) {
                    bienesAsignados.add(idBien);
                }
            } catch (Exception e) {
                errores.add("Bien " + idBien + ": " + e.getMessage());
            }
        }

        if (forzarReasignacion && !bienesYaAsignados.isEmpty()) {
            for (BienYaAsignadoDTO bienAsignado : bienesYaAsignados) {
                try {
                    ResultadoAsignacionDTO resultado = asignarBienAEstacion(
                            bienAsignado.getIdBien(), idEstacion, null, idUsuario, true);
                    if (resultado.isExito()) {
                        bienesAsignados.add(bienAsignado.getIdBien());
                    }
                } catch (Exception e) {
                    errores.add("Bien " + bienAsignado.getIdBien() + ": " + e.getMessage());
                }
            }
        }

        return ResultadoAsignacionMasivaDTO.builder()
                .totalBienes(idsBienes.size())
                .bienesLibresAsignados(bienesLibres.size())
                .bienesYaAsignados(bienesYaAsignados)
                .bienesAsignadosExitosamente(bienesAsignados)
                .errores(errores)
                .requiereConfirmacion(!bienesYaAsignados.isEmpty() && !forzarReasignacion)
                .build();
    }

    @Override
    public ResultadoDesasignacionDTO desasignarBienDeEstacion(Long idBien, Long idUsuario) {
        log.info("Desasignando bien {} de su estacion", idBien);

        Bien bien = buscarBienPorId(idBien);

        Optional<BienEstacion> asignacionOpt = bienEstacionRepository
                .findByBienIdAndFechaDesasignacionIsNull(idBien);

        if (asignacionOpt.isEmpty()) {
            return ResultadoDesasignacionDTO.builder()
                    .exito(false)
                    .mensaje("El bien no esta asignado a ninguna estacion")
                    .build();
        }

        BienEstacion asignacion = asignacionOpt.get();
        String nombreEstacionAnterior = asignacion.getEstacion().getNombre();

        asignacion.setFechaDesasignacion(LocalDateTime.now());
        asignacion.setObservaciones("Desasignado por usuario: " + idUsuario);
        bienEstacionRepository.saveAndFlush(asignacion);

        limpiarUbicacionDelBien(bien);

        log.info("Bien {} desasignado de estacion '{}'", idBien, nombreEstacionAnterior);

        return ResultadoDesasignacionDTO.builder()
                .exito(true)
                .mensaje("Bien desasignado de la estacion '" + nombreEstacionAnterior + "'")
                .nombreEstacionAnterior(nombreEstacionAnterior)
                .build();
    }

    @Override
    public ResultadoDesasignacionMasivaDTO desasignarBienesMasivos(List<Long> idsBienes, Long idUsuario) {
        log.info("Desasignacion masiva de {} bienes", idsBienes.size());

        List<Long> bienesDesasignados = new ArrayList<>();
        List<Long> bienesNoAsignados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (Long idBien : idsBienes) {
            try {
                ResultadoDesasignacionDTO resultado = desasignarBienDeEstacion(idBien, idUsuario);
                if (resultado.isExito()) {
                    bienesDesasignados.add(idBien);
                } else {
                    bienesNoAsignados.add(idBien);
                }
            } catch (Exception e) {
                errores.add("Bien " + idBien + ": " + e.getMessage());
                bienesNoAsignados.add(idBien);
            }
        }

        boolean exito = errores.isEmpty();
        String mensaje = construirMensajeDesasignacion(idsBienes.size(), bienesDesasignados.size(), errores.size());

        return ResultadoDesasignacionMasivaDTO.builder()
                .totalBienes(idsBienes.size())
                .bienesDesasignados(bienesDesasignados)
                .bienesNoAsignados(bienesNoAsignados)
                .errores(errores)
                .exito(exito)
                .mensaje(mensaje)
                .build();
    }

    @Override
    public ResultadoDesasignacionMasivaDTO desagruparBienesDeEstacion(
            Long idEstacion,
            List<Long> idsBienes,
            Long idUsuario) {

        log.info("Desagrupando {} bienes de estacion {}", idsBienes.size(), idEstacion);

        Estacion estacion = buscarEstacionPorId(idEstacion);

        List<Long> bienesValidados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (Long idBien : idsBienes) {
            Optional<BienEstacion> asignacion = bienEstacionRepository.findByBienIdAndFechaDesasignacionIsNull(idBien);
            if (asignacion.isPresent()) {
                if (asignacion.get().getEstacion().getIdEstacion().equals(idEstacion)) {
                    bienesValidados.add(idBien);
                } else {
                    errores.add("Bien " + idBien + " no esta en la estacion especificada");
                }
            } else {
                errores.add("Bien " + idBien + " no esta asignado");
            }
        }

        ResultadoDesasignacionMasivaDTO resultado = desasignarBienesMasivos(bienesValidados, idUsuario);
        resultado.getErrores().addAll(errores);
        resultado.setMensaje("Desagrupacion de '" + estacion.getNombre() + "': " + resultado.getMensaje());

        return resultado;
    }

    @Override
    public ResultadoGestionBienesDTO gestionarBienesEstacion(
            Long idEstacion,
            List<Long> bienesAAgrupar,
            List<Long> bienesADesagrupar,
            Long idUsuario,
            boolean forzarReasignacion) {

        log.info("Gestion combinada para estacion {} (forzar={})", idEstacion, forzarReasignacion);

        Estacion estacion = buscarEstacionPorId(idEstacion);

        ResultadoAsignacionMasivaDTO resultadoAgrupacion = null;
        ResultadoDesasignacionMasivaDTO resultadoDesagrupacion = null;
        List<BienYaAsignadoDTO> bienesEnConflicto = new ArrayList<>();
        boolean requiereConfirmacion = false;
        boolean exito = true;
        StringBuilder mensaje = new StringBuilder();

        if (bienesADesagrupar != null && !bienesADesagrupar.isEmpty()) {
            try {
                resultadoDesagrupacion = desagruparBienesDeEstacion(idEstacion, bienesADesagrupar, idUsuario);
                if (!resultadoDesagrupacion.isExito()) exito = false;
                mensaje.append("Desagrupacion: ").append(resultadoDesagrupacion.getMensaje()).append("; ");
            } catch (Exception e) {
                exito = false;
                mensaje.append("Error en desagrupacion: ").append(e.getMessage()).append("; ");
            }
        }

        if (bienesAAgrupar != null && !bienesAAgrupar.isEmpty()) {
            try {
                resultadoAgrupacion = asignarBienesMasivos(bienesAAgrupar, idEstacion, idUsuario, forzarReasignacion);
                
                if (resultadoAgrupacion.isRequiereConfirmacion()) {
                    bienesEnConflicto = resultadoAgrupacion.getBienesYaAsignados();
                    requiereConfirmacion = true;
                    exito = false;
                    mensaje.append(bienesEnConflicto.size())
                           .append(" bien(es) ya están en otras estaciones. ¿Confirmar reasignación?");
                } else {
                    mensaje.append("Agrupacion: ").append(resultadoAgrupacion.getBienesAsignadosExitosamente().size())
                           .append(" bienes asignados");
                    if (!resultadoAgrupacion.getErrores().isEmpty()) {
                        exito = false;
                    }
                }
            } catch (Exception e) {
                exito = false;
                mensaje.append("Error en agrupacion: ").append(e.getMessage());
            }
        }

        return ResultadoGestionBienesDTO.builder()
                .idEstacion(idEstacion)
                .nombreEstacion(estacion.getNombre())
                .resultadoAgrupacion(resultadoAgrupacion)
                .resultadoDesagrupacion(resultadoDesagrupacion)
                .bienesEnConflicto(bienesEnConflicto)
                .requiereConfirmacion(requiereConfirmacion)
                .exito(exito)
                .mensaje(exito ? "Gestion completada" : mensaje.toString())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Estacion> obtenerEstacionActual(Long idBien) {
        return bienEstacionRepository.findByBienIdAndFechaDesasignacionIsNull(idBien)
                .map(BienEstacion::getEstacion);
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosticoBienDTO diagnosticarEstadoBien(Long idBien) {
        log.info("Ejecutando diagnostico para bien {}", idBien);

        Optional<Bien> bienOpt = bienRepository.findById(idBien);
        if (bienOpt.isEmpty()) {
            return DiagnosticoBienDTO.builder()
                    .idBien(idBien)
                    .bienExiste(false)
                    .mensaje("El bien no existe")
                    .build();
        }

        List<BienEstacion> todasAsignaciones = bienEstacionRepository.findByBienIdOrderByFechaAsignacionDesc(idBien);
        Optional<BienEstacion> asignacionActiva = bienEstacionRepository.findByBienIdAndFechaDesasignacionIsNull(idBien);

        long asignacionesActivas = todasAsignaciones.stream()
                .filter(be -> be.getFechaDesasignacion() == null)
                .count();

        boolean hayInconsistencias = asignacionesActivas > 1;
        String mensaje = construirMensajeDiagnostico(asignacionesActivas, todasAsignaciones.size());

        return DiagnosticoBienDTO.builder()
                .idBien(idBien)
                .bienExiste(true)
                .tieneAsignacionActiva(asignacionActiva.isPresent())
                .cantidadAsignacionesActivas((int) asignacionesActivas)
                .cantidadAsignacionesTotales(todasAsignaciones.size())
                .hayInconsistencias(hayInconsistencias)
                .mensaje(mensaje)
                .nombreEstacionActual(asignacionActiva.map(a -> a.getEstacion().getNombre()).orElse(null))
                .build();
    }

    private Bien buscarBienPorId(Long idBien) {
        return bienRepository.findById(idBien)
                .orElseThrow(() -> new RuntimeException("Bien no encontrado: " + idBien));
    }

    private Estacion buscarEstacionPorId(Long idEstacion) {
        return estacionRepository.findById(idEstacion)
                .orElseThrow(() -> new RuntimeException("Estacion no encontrada: " + idEstacion));
    }

    private void validarCapacidadEstacion(Estacion estacion) {
        if (estacion.getCapacidadBienes() != null) {
            long bienesActuales = bienEstacionRepository.countByEstacionIdAndFechaDesasignacionIsNull(estacion.getIdEstacion());
            if (bienesActuales >= estacion.getCapacidadBienes()) {
                throw new RuntimeException("La estacion '" + estacion.getNombre() +
                        "' ha alcanzado su capacidad maxima (" + estacion.getCapacidadBienes() + ")");
            }
        }
    }

    private void actualizarUbicacionDesdeEstacion(Bien bien, Estacion estacion) {
        Ambiente ambiente = ambienteRepository.findById(estacion.getAmbiente().getIdAmbiente())
                .orElseThrow(() -> new RuntimeException("Ambiente no encontrado"));
        Piso piso = pisoRepository.findById(ambiente.getPiso().getIdPiso())
                .orElseThrow(() -> new RuntimeException("Piso no encontrado"));
        Edificio edificio = edificioRepository.findById(piso.getEdificio().getIdEdificio())
                .orElseThrow(() -> new RuntimeException("Edificio no encontrado"));

        String nombreUbicacion = String.format("%s → %s → %s → %s",
                edificio.getNombre(), piso.getNombre(), ambiente.getNombre(), estacion.getNombre());

        Ubicacion ubicacion = ubicacionRepository.findByIdEstacion(estacion.getIdEstacion())
                .orElseGet(() -> {
                    Ubicacion nueva = new Ubicacion();
                    nueva.setNombreUbicacion(nombreUbicacion);
                    nueva.setIdEdificio(edificio.getIdEdificio());
                    nueva.setIdPiso(piso.getIdPiso());
                    nueva.setIdAmbiente(ambiente.getIdAmbiente());
                    nueva.setIdEstacion(estacion.getIdEstacion());
                    return ubicacionRepository.save(nueva);
                });

        if (!ubicacion.getNombreUbicacion().equals(nombreUbicacion)) {
            ubicacion.setNombreUbicacion(nombreUbicacion);
            ubicacionRepository.save(ubicacion);
        }

        bien.setUbicacionActual(ubicacion);
        bienRepository.save(bien);
    }

    private void limpiarUbicacionDelBien(Bien bien) {
        Ubicacion ubicacionGenerica = ubicacionRepository
                .findByNombreUbicacionAndIdEstacionIsNull("Sin Asignar")
                .orElseGet(() -> {
                    Ubicacion nueva = new Ubicacion();
                    nueva.setNombreUbicacion("Sin Asignar");
                    return ubicacionRepository.save(nueva);
                });

        bien.setUbicacionActual(ubicacionGenerica);
        bienRepository.save(bien);
    }

    private String construirMensajeDesasignacion(int total, int desasignados, int errores) {
        if (errores == 0) {
            return String.format("Desasignacion exitosa: %d/%d bienes", desasignados, total);
        }
        return String.format("Desasignacion con errores: %d desasignados, %d errores de %d",
                desasignados, errores, total);
    }

    private String construirMensajeDiagnostico(long activas, int totales) {
        if (activas == 0) {
            return "Bien libre (" + totales + " historicas)";
        } else if (activas == 1) {
            return "Bien asignado correctamente (" + totales + " historicas)";
        }
        return "INCONSISTENCIA - " + activas + " asignaciones activas";
    }
}