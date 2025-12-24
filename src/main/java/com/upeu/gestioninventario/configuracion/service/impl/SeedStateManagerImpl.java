package com.upeu.gestioninventario.configuracion.service.impl;

import com.upeu.gestioninventario.configuracion.model.SeedFileState;
import com.upeu.gestioninventario.configuracion.repository.SeedFileStateRepository;
import com.upeu.gestioninventario.configuracion.service.ISeedStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeedStateManagerImpl implements ISeedStateManager {

    private final SeedFileStateRepository repository;
    private final Map<String, Boolean> archivosCambiados = new ConcurrentHashMap<>();


    @Override
    public boolean necesitaRecarga(String nombreArchivo, String hashActual) {
        if (hashActual == null || hashActual.isBlank()) {
            log.warn("Hash vacio para archivo: {}. Se forzara recarga.", nombreArchivo);
            return true;
        }

        Optional<SeedFileState> estadoExistente = repository.findByNombreArchivo(nombreArchivo);

        if (estadoExistente.isEmpty()) {
            log.info("Archivo nuevo detectado: {}", nombreArchivo);
            archivosCambiados.put(nombreArchivo, true);
            return true;
        }

        String hashGuardado = estadoExistente.get().getHashContenido();
        boolean cambio = !hashActual.equals(hashGuardado);

        if (cambio) {
            log.info("Cambio detectado en archivo: {} (hash anterior: {}, hash actual: {})",
                    nombreArchivo,
                    hashGuardado.substring(0, 8) + "...",
                    hashActual.substring(0, 8) + "...");
            archivosCambiados.put(nombreArchivo, true);
        } else {
            log.debug("Sin cambios en archivo: {}", nombreArchivo);
            archivosCambiados.put(nombreArchivo, false);
        }

        return cambio;
    }

    @Override
    @Transactional
    public void registrarCarga(String nombreArchivo, String rutaArchivo, String tipoSeed, String hashContenido) {
        Optional<SeedFileState> existente = repository.findByNombreArchivo(nombreArchivo);

        if (existente.isPresent()) {
            SeedFileState estado = existente.get();
            estado.setHashContenido(hashContenido);
            repository.save(estado);
            log.debug("Estado actualizado para: {} (recarga #{})", nombreArchivo, estado.getCantidadRecargas());
        } else {
            SeedFileState nuevoEstado = SeedFileState.builder()
                    .nombreArchivo(nombreArchivo)
                    .rutaArchivo(rutaArchivo)
                    .tipoSeed(tipoSeed)
                    .hashContenido(hashContenido)
                    .build();
            repository.save(nuevoEstado);
            log.info("Nuevo archivo registrado: {} (tipo: {})", nombreArchivo, tipoSeed);
        }

        archivosCambiados.remove(nombreArchivo);
    }
}
