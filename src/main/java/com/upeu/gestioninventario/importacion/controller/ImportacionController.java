package com.upeu.gestioninventario.importacion.controller;

import com.upeu.gestioninventario.importacion.dto.ImportacionResultadoDTO;
import com.upeu.gestioninventario.importacion.dto.ImportacionConfirmacionInput;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.RespuestaImportacionDTO;
import com.upeu.gestioninventario.importacion.service.IImportacionService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/importacion")
@RequiredArgsConstructor
@Slf4j
public class ImportacionController {

    private final IImportacionService importacionService;

    @PostMapping("/analizar")
    public ResponseEntity<OperacionResultadoDTO<RespuestaImportacionDTO>> analizarArchivo(
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("REST POST: /analizar - Archivo: {}", file.getOriginalFilename());
            RespuestaImportacionDTO respuesta = importacionService.analizarArchivo(file);

            if (!respuesta.esArchivoValido()) {
                return ResponseEntity.ok(OperacionResultadoDTO.error(
                        "El archivo contiene errores de validación"
                ));
            }

            return ResponseEntity.ok(OperacionResultadoDTO.exito(
                    "Archivo analizado exitosamente",
                    respuesta
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación en archivo: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    OperacionResultadoDTO.error("Archivo inválido: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error al analizar archivo: {}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError().body(
                    OperacionResultadoDTO.error("Error al procesar el archivo")
            );
        }
    }

    @PostMapping("/confirmar")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResponseEntity<OperacionResultadoDTO<ImportacionResultadoDTO>> confirmarImportacion(
            @Valid @RequestBody ImportacionConfirmacionInput confirmacion) {
        try {
            log.info("REST POST: /confirmar - Sesión: {}", confirmacion.importacionSesionId());
            ImportacionResultadoDTO resultado = importacionService.ejecutarImportacionConfirmada(confirmacion);

            String mensaje = String.format(
                    "Importación completada - Creados: %d, Actualizados: %d, Errores: %d",
                    resultado.bienesCreados(),
                    resultado.bienesActualizados(),
                    resultado.filasConError()
            );

            return ResponseEntity.ok(OperacionResultadoDTO.exito(mensaje, resultado));
            
        } catch (IllegalArgumentException e) {
            log.error("Sesión de importación inválida: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    OperacionResultadoDTO.error("Sesión de importación inválida o expirada")
            );
        } catch (Exception e) {
            log.error("Error al ejecutar importación", e);
            return ResponseEntity.internalServerError().body(
                    OperacionResultadoDTO.error("Error al procesar la importación")
            );
        }
    }
}