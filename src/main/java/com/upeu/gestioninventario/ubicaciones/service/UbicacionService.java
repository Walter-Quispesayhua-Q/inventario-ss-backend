package com.upeu.gestioninventario.ubicaciones.service;

import com.upeu.gestioninventario.ubicaciones.dto.UbicacionCreacionInput;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import com.upeu.gestioninventario.ubicaciones.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UbicacionService {
    private final UbicacionRepository ubicacionRepository;

    @Transactional
    public Ubicacion creacionUbicacion(UbicacionCreacionInput dataUbicacion) {
        if (dataUbicacion.edificio() != null && !dataUbicacion.edificio().isBlank()) {
            log.info("procesando ubicacion detallada: {} - {} - {}", dataUbicacion.edificio(), dataUbicacion.piso(),  dataUbicacion.oficinaAmbiente());
            Optional<Ubicacion> existencia = ubicacionRepository.findByEdificioAndPisoAndOficinaAmbiente(
                    dataUbicacion.edificio(), dataUbicacion.piso(),  dataUbicacion.oficinaAmbiente()
            );
            if (existencia.isPresent()) {
                log.info("ubicacion existente: {}", existencia.get());
                return existencia.get();
            }
            log.info("creando ubicacion ");
            String nombreCompleto = Stream.of(dataUbicacion.edificio(), dataUbicacion.piso(), dataUbicacion.oficinaAmbiente())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" - "));
            Ubicacion nuevaUbicacion = Ubicacion.builder()
                    .nombreUbicacion(nombreCompleto)
                    .descripcion(dataUbicacion.descripcion())
                    .edificio(dataUbicacion.edificio())
                    .piso(dataUbicacion.piso())
                    .oficinaAmbiente(dataUbicacion.oficinaAmbiente())
                    .build();
            return ubicacionRepository.save(nuevaUbicacion);
        }
        if (dataUbicacion.nombreSimple() != null && !dataUbicacion.nombreSimple().isBlank()) {
            log.info("Procesando ubicacion simple: {}", dataUbicacion.nombreSimple());
            return ubicacionRepository.findByNombreUbicacion(dataUbicacion.nombreSimple())
                    .orElseGet(
                            () -> {
                                log.info("creando ubicacion simple");
                                Ubicacion nuevaUbicacion = Ubicacion.builder()
                                        .nombreUbicacion(dataUbicacion.nombreSimple())
                                        .descripcion(dataUbicacion.descripcion())
                                        .build();
                                return  ubicacionRepository.save(nuevaUbicacion);
                            }
                    );
        }
        throw new IllegalArgumentException("Los datos para la ubicación son insuficientes. Proporcione un nombre simple o detalles de la ubicación.");
    }
}
