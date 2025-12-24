package com.upeu.gestioninventario.importacion.service.cache;

import com.upeu.gestioninventario.importacion.dto.SesionAnalisisV3DTO;

import java.util.Optional;

public interface ISesionAnalisisCache {

    String guardarSesion(SesionAnalisisV3DTO sesion);

    Optional<SesionAnalisisV3DTO> obtenerYEliminarSesion(String sessionId);
}
