package com.upeu.gestioninventario.importacion.service.persistencia.impl;

import com.upeu.gestioninventario.importacion.dto.ImportacionConfirmacionInput;
import com.upeu.gestioninventario.importacion.dto.ImportacionResultadoDTO;
import com.upeu.gestioninventario.importacion.dto.SesionAnalisisV3DTO;
import com.upeu.gestioninventario.importacion.service.cache.ISesionAnalisisCache;
import com.upeu.gestioninventario.importacion.service.persistencia.IConfirmacionImportacionService;
import com.upeu.gestioninventario.importacion.service.persistencia.IPersistencia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmacionImportacionServiceImpl implements IConfirmacionImportacionService {

    private final ISesionAnalisisCache sesionAnalisisCache;
    private final IPersistencia persistenciaV3;

    @Override
    public ImportacionResultadoDTO confirmarImportacion(ImportacionConfirmacionInput confirmacion) {
        log.info("Recibida confirmacion de importacion");

        validarConfirmacion(confirmacion);

        SesionAnalisisV3DTO sesion = obtenerSesionDelCache(confirmacion.importacionSesionId());
        
        int totalHojas = sesion.hojas() != null ? sesion.hojas().size() : 0;
        int totalFilas = sesion.todasLasFilas().size();
        int totalEstaciones = sesion.todasLasEstaciones().size();

        log.info("Sesion recuperada - Archivo: {}, Hojas: {}, Filas: {}, Estaciones: {}",
                sesion.nombreArchivo(), totalHojas, totalFilas, totalEstaciones);

        return persistenciaV3.ejecutar(sesion);
    }

    private void validarConfirmacion(ImportacionConfirmacionInput confirmacion) {
        if (confirmacion == null) {
            throw new IllegalArgumentException("La confirmacion no puede ser nula");
        }

        if (confirmacion.importacionSesionId() == null || confirmacion.importacionSesionId().isBlank()) {
            throw new IllegalArgumentException("El ID de sesion de importacion es requerido");
        }

        log.info("Confirmacion validada - SessionId: {}, GruposEnviados: {}",
                confirmacion.importacionSesionId(),
                confirmacion.tieneGrupos());
    }

    private SesionAnalisisV3DTO obtenerSesionDelCache(String sessionId) {
        return sesionAnalisisCache.obtenerYEliminarSesion(sessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La sesion de importacion ha expirado o no existe"
                ));
    }
}