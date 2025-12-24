package com.upeu.gestioninventario.importacion.service.persistencia;

import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;

public interface IResolverUbicacionService {

    Ubicacion resolverUbicacion(AmbienteDetectadoDTO ambienteDetectado, String textoUbicacionOriginal);
}