package com.upeu.gestioninventario.estructuras.controller;

import com.upeu.gestioninventario.estructuras.dto.ConfirmacionOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioConPisosResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioInputDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroEdificioDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoInputDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.ResultadoOperacionDTO;
import com.upeu.gestioninventario.estructuras.service.IEdificioService;
import com.upeu.gestioninventario.estructuras.service.IEstructuraCoordinadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EdificioController {

    private final IEdificioService edificioService;
    private final IEstructuraCoordinadorService coordinadorService;

    @QueryMapping
    public List<EdificioDTO> edificios() {
        log.info("GraphQL Query: edificios");
        return edificioService.listarEdificios();
    }

    @QueryMapping
    public EdificioDTO edificioPorCodigo(@Argument String codigo) {
        log.info("GraphQL Query: edificioPorCodigo({})", codigo);
        return edificioService.obtenerEdificioPorCodigo(codigo);
    }

    @QueryMapping
    public EdificioConPisosResponseDTO edificioConPisos(@Argument Long idEdificio) {
        log.debug("GraphQL Query: edificioConPisos({})", idEdificio);
        return coordinadorService.obtenerEdificioConPisos(idEdificio);
    }

    @QueryMapping
    public List<EdificioDTO> buscarEdificios(@Argument FiltroEdificioDTO filtro) {
        log.info("GraphQL Query: buscarEdificios({})", filtro);
        return edificioService.buscarEdificiosConFiltros(filtro);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public EdificioConPisosResponseDTO crearEdificio(@Argument EdificioInputDTO input) {
        log.info("GraphQL Mutation: crearEdificio(nombre={})", input.getNombre());
        return coordinadorService.crearEdificioConPisos(input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public EdificioConPisosResponseDTO actualizarEdificio(
            @Argument Long idEdificio,
            @Argument EdificioInputDTO input) {
        log.info("GraphQL Mutation: actualizarEdificio({})", idEdificio);
        return coordinadorService.actualizarEdificioConPisos(idEdificio, input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public PisoResumenDTO agregarPisoAEdificio(
            @Argument Long idEdificio,
            @Argument PisoInputDTO input) {
        log.info("GraphQL Mutation: agregarPisoAEdificio({}, piso={})", idEdificio, input.getNombre());
        return coordinadorService.agregarPisoAEdificio(idEdificio, input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoOperacionDTO eliminarEdificio(
            @Argument Long idEdificio,
            @Argument ConfirmacionOperacionDTO confirmacion) {
        log.info("GraphQL Mutation: eliminarEdificio({}, confirmado={})", 
                 idEdificio, confirmacion != null && confirmacion.confirmado());
        return coordinadorService.eliminarEdificio(idEdificio, confirmacion);
    }
}
