package com.upeu.gestioninventario.importacion.service.persistencia;

import com.upeu.gestioninventario.importacion.dto.ImportacionConfirmacionInput;
import com.upeu.gestioninventario.importacion.dto.ImportacionResultadoDTO;

public interface IConfirmacionImportacionService {

    ImportacionResultadoDTO confirmarImportacion(ImportacionConfirmacionInput confirmacion);
}