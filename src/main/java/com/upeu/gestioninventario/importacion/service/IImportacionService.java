package com.upeu.gestioninventario.importacion.service;

import com.upeu.gestioninventario.importacion.dto.ImportacionResultadoDTO;
import com.upeu.gestioninventario.importacion.dto.ImportacionConfirmacionInput;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.RespuestaImportacionDTO;
import org.springframework.web.multipart.MultipartFile;


public interface IImportacionService {
    RespuestaImportacionDTO analizarArchivo(MultipartFile file);

    ImportacionResultadoDTO ejecutarImportacionConfirmada(ImportacionConfirmacionInput confirmacion);
}
