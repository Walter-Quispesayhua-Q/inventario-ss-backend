package com.upeu.gestioninventario.estructuras.service;

import com.upeu.gestioninventario.estructuras.dto.asignacion.*;
import com.upeu.gestioninventario.estructuras.model.Estacion;

import java.util.List;
import java.util.Optional;

public interface IAsignacionEstacionService {

    ResultadoAsignacionDTO asignarBienAEstacion(
            Long idBien,
            Long idEstacion,
            String posicionRelativa,
            Long idUsuario,
            boolean forzarReasignacion);

    ResultadoAsignacionMasivaDTO asignarBienesMasivos(
            List<Long> idsBienes,
            Long idEstacion,
            Long idUsuario,
            boolean forzarReasignacion);

    ResultadoDesasignacionDTO desasignarBienDeEstacion(Long idBien, Long idUsuario);

    ResultadoDesasignacionMasivaDTO desasignarBienesMasivos(List<Long> idsBienes, Long idUsuario);

    ResultadoDesasignacionMasivaDTO desagruparBienesDeEstacion(
            Long idEstacion,
            List<Long> idsBienes,
            Long idUsuario);

    ResultadoGestionBienesDTO gestionarBienesEstacion(
            Long idEstacion,
            List<Long> bienesAAgrupar,
            List<Long> bienesADesagrupar,
            Long idUsuario,
            boolean forzarReasignacion);

    Optional<Estacion> obtenerEstacionActual(Long idBien);

    DiagnosticoBienDTO diagnosticarEstadoBien(Long idBien);
}