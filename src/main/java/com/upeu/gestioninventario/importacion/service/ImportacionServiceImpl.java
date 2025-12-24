package com.upeu.gestioninventario.importacion.service;

import com.upeu.gestioninventario.importacion.dto.ImportacionResultadoDTO;
import com.upeu.gestioninventario.importacion.dto.ImportacionConfirmacionInput;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.RespuestaImportacionDTO;
import com.upeu.gestioninventario.importacion.service.analisis.IAnalizadorArchivo;
import com.upeu.gestioninventario.importacion.service.persistencia.IConfirmacionImportacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportacionServiceImpl implements IImportacionService {

    private final IAnalizadorArchivo analizadorArchivo;
    private final IConfirmacionImportacionService confirmacionImportacionService;

    @Override
    public RespuestaImportacionDTO analizarArchivo(MultipartFile file) {
        try {
            return analizadorArchivo.analizarArchivo(file);
        } catch (Exception e) {
            log.error("Error al analizar archivo: {}", e.getMessage(), e);
            return RespuestaImportacionDTO.archivoInvalido(
                    file.getOriginalFilename(),
                    "Error al procesar archivo: " + e.getMessage()
            );
        }
    }

    @Override
    public ImportacionResultadoDTO ejecutarImportacionConfirmada(ImportacionConfirmacionInput confirmacion) {
        return confirmacionImportacionService.confirmarImportacion(confirmacion);
    }
}