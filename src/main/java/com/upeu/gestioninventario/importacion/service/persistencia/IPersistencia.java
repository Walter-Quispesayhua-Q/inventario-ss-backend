package com.upeu.gestioninventario.importacion.service.persistencia;

import com.upeu.gestioninventario.importacion.dto.ImportacionResultadoDTO;
import com.upeu.gestioninventario.importacion.dto.SesionAnalisisV3DTO;

public interface IPersistencia {

    ImportacionResultadoDTO ejecutar(SesionAnalisisV3DTO sesion);
}
