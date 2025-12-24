package com.upeu.gestioninventario.ubicaciones.controller;

import com.upeu.gestioninventario.ubicaciones.dto.UbicacionDTO;
import com.upeu.gestioninventario.ubicaciones.mapper.UbicacionMapper;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import com.upeu.gestioninventario.ubicaciones.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UbicacionController {

    private final UbicacionRepository ubicacionRepository;
    private final UbicacionMapper ubicacionMapper;

    @QueryMapping
    public List<UbicacionDTO> ubicaciones() {
        log.debug("GraphQL Query: ubicaciones (sin estaciones)");
        
        List<Ubicacion> ubicacionesSinEstacion = ubicacionRepository.findAllByIdEstacionIsNull();
        
        return ubicacionesSinEstacion.stream()
                .map(ubicacionMapper::toDto)
                .toList();
    }

    @QueryMapping
    public UbicacionDTO ubicacionPorId(@Argument Long id) {
        log.debug("GraphQL Query: ubicacionPorId({})", id);
        
        return ubicacionRepository.findById(id)
                .map(ubicacionMapper::toDto)
                .orElse(null);
    }
}
