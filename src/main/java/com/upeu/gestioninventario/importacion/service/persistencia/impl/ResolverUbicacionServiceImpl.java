package com.upeu.gestioninventario.importacion.service.persistencia.impl;

import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverUbicacionService;
import com.upeu.gestioninventario.ubicaciones.dto.UbicacionCreacionInput;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import com.upeu.gestioninventario.ubicaciones.repository.UbicacionRepository;
import com.upeu.gestioninventario.ubicaciones.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolverUbicacionServiceImpl implements IResolverUbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final UbicacionService ubicacionService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Ubicacion resolverUbicacion(AmbienteDetectadoDTO ambienteDetectado, String textoUbicacionOriginal) {

        if (ambienteDetectado != null && ambienteDetectado.tieneAmbiente()) {
            return resolverConAmbiente(ambienteDetectado);
        }

        return resolverUbicacionPlana(textoUbicacionOriginal);
    }

    private Ubicacion resolverConAmbiente(AmbienteDetectadoDTO ambiente) {
        log.info("Resolviendo ubicacion con ambiente detectado: {} (ID: {})",
                ambiente.nombreAmbiente(), ambiente.idAmbiente());

        return ubicacionRepository.findByIdAmbiente(ambiente.idAmbiente())
                .orElseGet(() -> {
                    log.info("Creando ubicacion vinculada al ambiente: {}", ambiente.nombreAmbiente());
                    Ubicacion nuevaUbicacion = Ubicacion.builder()
                            .nombreUbicacion(ambiente.obtenerJerarquiaCompleta())
                            .idAmbiente(ambiente.idAmbiente())
                            .edificio(ambiente.nombreEdificio())
                            .piso(ambiente.nombrePiso())
                            .oficinaAmbiente(ambiente.nombreAmbiente())
                            .descripcion("Ubicacion creada desde importacion")
                            .build();
                    return ubicacionRepository.save(nuevaUbicacion);
                });
    }

    private Ubicacion resolverUbicacionPlana(String nombreUbicacion) {
        if (nombreUbicacion == null || nombreUbicacion.isBlank()) {
            nombreUbicacion = "Sin ubicacion especificada";
        }

        String nombreNormalizado = nombreUbicacion.trim();
        log.info("Resolviendo ubicacion plana: '{}'", nombreNormalizado);

        return ubicacionRepository.findByNombreUbicacion(nombreNormalizado)
                .orElseGet(() -> {
                    log.info("Creando ubicacion plana: {}", nombreNormalizado);
                    UbicacionCreacionInput input = new UbicacionCreacionInput(
                            nombreNormalizado, null, null, null,
                            "Ubicacion creada desde importacion"
                    );
                    return ubicacionService.creacionUbicacion(input);
                });
    }
}