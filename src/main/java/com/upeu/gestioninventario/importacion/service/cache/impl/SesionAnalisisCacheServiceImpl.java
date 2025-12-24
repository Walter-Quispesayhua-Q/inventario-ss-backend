package com.upeu.gestioninventario.importacion.service.cache.impl;

import com.upeu.gestioninventario.importacion.dto.SesionAnalisisV3DTO;
import com.upeu.gestioninventario.importacion.service.cache.ISesionAnalisisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SesionAnalisisCacheServiceImpl implements ISesionAnalisisCache {

    private static final long TTL_MILLIS = 30L * 60L * 1000L;

    private record EntradaCacheV3(SesionAnalisisV3DTO sesion, long expiraEnMillis) {}
    private final Map<String, EntradaCacheV3> cacheV3 = new ConcurrentHashMap<>();

    @Override
    public String guardarSesion(SesionAnalisisV3DTO sesion) {
        String sessionId = UUID.randomUUID().toString();
        purgarExpirados();
        long expira = System.currentTimeMillis() + TTL_MILLIS;

        SesionAnalisisV3DTO sesionConId = new SesionAnalisisV3DTO(
                sessionId,
                sesion.nombreArchivo(),
                sesion.hojas(),
                sesion.categoriasDetectadas()
        );

        cacheV3.put(sessionId, new EntradaCacheV3(sesionConId, expira));

        int totalHojas = sesion.hojas() != null ? sesion.hojas().size() : 0;
        int totalFilas = sesion.todasLasFilas().size();
        int totalEstaciones = sesion.todasLasEstaciones().size();

        log.info("Sesion guardada - ID: {}, Hojas: {}, Filas: {}, Estaciones: {}, TTL: {} min",
                sessionId, totalHojas, totalFilas, totalEstaciones, TTL_MILLIS / 60000);

        return sessionId;
    }

    @Override
    public Optional<SesionAnalisisV3DTO> obtenerYEliminarSesion(String sessionId) {
        log.info("Recuperando y eliminando sesion: {}", sessionId);
        EntradaCacheV3 entrada = cacheV3.remove(sessionId);

        if (entrada == null) {
            log.warn("Sesion '{}' no encontrada", sessionId);
            return Optional.empty();
        }

        if (System.currentTimeMillis() > entrada.expiraEnMillis()) {
            log.warn("Sesion '{}' expirada", sessionId);
            return Optional.empty();
        }

        int totalHojas = entrada.sesion().hojas() != null ? entrada.sesion().hojas().size() : 0;
        log.info("Sesion '{}' recuperada - {} hojas", sessionId, totalHojas);

        return Optional.of(entrada.sesion());
    }

    private void purgarExpirados() {
        long ahora = System.currentTimeMillis();
        int eliminados = 0;

        var iteratorV3 = cacheV3.entrySet().iterator();
        while (iteratorV3.hasNext()) {
            if (iteratorV3.next().getValue().expiraEnMillis() < ahora) {
                iteratorV3.remove();
                eliminados++;
            }
        }

        if (eliminados > 0) {
            log.debug("Purgadas {} sesiones expiradas", eliminados);
        }
    }
}
