package com.upeu.gestioninventario.importacion.service.analisis;

import com.upeu.gestioninventario.importacion.dto.formato.respuesta.RespuestaImportacionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IAnalizadorArchivo {

    RespuestaImportacionDTO analizarArchivo(MultipartFile archivo) throws IOException;
}
