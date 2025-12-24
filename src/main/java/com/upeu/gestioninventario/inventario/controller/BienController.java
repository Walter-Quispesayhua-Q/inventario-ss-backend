package com.upeu.gestioninventario.inventario.controller;

import com.upeu.gestioninventario.inventario.dto.bien.*;
import com.upeu.gestioninventario.inventario.service.IBienService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BienController {

    private final IBienService bienService;

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public OperacionResultadoDTO<BienDTO> crearBien(@Argument("bien") @Valid BienCreacionInput input) {
        log.info("GraphQL Mutation: crearBien (campos: {})", input.campos() != null ? input.campos().size() : 0);
        return bienService.crearBien(input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public OperacionResultadoDTO<BienDTO> actualizarBien(
            @Argument Long id,
            @Argument("bien") @Valid BienActualizacionInput input) {
        log.info("GraphQL Mutation: actualizarBien({})", id);
        return bienService.actualizarBien(id, input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public OperacionResultadoDTO<BienEliminacionResultadoDTO> eliminarBien(@Argument Long id) {
        log.info("GraphQL Mutation: eliminarBien({})", id);
        return bienService.eliminarBien(id);
    }

    @QueryMapping
    public BienesPaginados obtenerBienes(
            @Argument Integer page,
            @Argument Integer limit,
            @Argument String buscar,
            @Argument Long categoriaId,
            @Argument Long ubicacionId,
            @Argument String estadoOperacional,
            @Argument String estadoFisico,
            @Argument Long departamentoId,
            @Argument Long responsableId,
            @Argument String ordenarPor,
            @Argument String orden) {
        log.debug("GraphQL Query: obtenerBienes(page={}, limit={})", page, limit);
        return bienService.obtenerBienesPaginados(
                page, limit, buscar, categoriaId, ubicacionId,
                estadoOperacional, estadoFisico, departamentoId,
                responsableId, ordenarPor, orden
        );
    }

    @QueryMapping
    public BienDTO bienPorId(@Argument Long id) {
        log.debug("GraphQL Query: bienPorId({})", id);
        return bienService.obtenerBienPorId(id);
    }

    @QueryMapping
    public List<BienDTO> buscarBienes(@Argument String termino) {
        log.debug("GraphQL Query: buscarBienes(termino={})", termino);
        return bienService.buscarBienes(termino);
    }

    @QueryMapping
    public List<BienDTO> bienesPorCategoria(@Argument Long categoriaId) {
        log.debug("GraphQL Query: bienesPorCategoria({})", categoriaId);
        return bienService.obtenerBienesPorCategoria(categoriaId);
    }
}