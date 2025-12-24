package com.upeu.gestioninventario.estructuras.controller;

import com.upeu.gestioninventario.estructuras.dto.ConfirmacionOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroAmbienteDTO;
import com.upeu.gestioninventario.estructuras.dto.ResultadoOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteInputDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionInputDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionResumenDTO;
import com.upeu.gestioninventario.estructuras.service.IAmbienteService;
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
public class AmbienteController {

    private final IAmbienteService ambienteService;
    private final IEstructuraCoordinadorService coordinadorService;

    @QueryMapping
    public List<AmbienteDTO> ambientes() {
        log.info("GraphQL Query: ambientes");
        return ambienteService.listarTodosAmbientes();
    }

    @QueryMapping
    public List<AmbienteDTO> ambientesPorDepartamento(@Argument Long idDepartamento) {
        log.info("GraphQL Query: ambientesPorDepartamento({})", idDepartamento);
        return ambienteService.obtenerAmbientesPorDepartamento(idDepartamento);
    }

    @QueryMapping
    public List<AmbienteDTO> buscarAmbientes(@Argument FiltroAmbienteDTO filtro) {
        log.info("GraphQL Query: buscarAmbientes({})", filtro);
        return ambienteService.buscarAmbientesConFiltros(filtro);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public AmbienteResumenDTO actualizarAmbiente(
            @Argument Long idAmbiente,
            @Argument AmbienteInputDTO input,
            @Argument ConfirmacionOperacionDTO confirmacion) {
        log.info("GraphQL Mutation: actualizarAmbiente({}, confirmado={})", 
                 idAmbiente, confirmacion != null && confirmacion.confirmado());
        return coordinadorService.actualizarAmbiente(idAmbiente, input, confirmacion);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoOperacionDTO eliminarAmbiente(
            @Argument Long idAmbiente,
            @Argument ConfirmacionOperacionDTO confirmacion) {
        log.info("GraphQL Mutation: eliminarAmbiente({}, confirmado={})", 
                 idAmbiente, confirmacion != null && confirmacion.confirmado());
        return coordinadorService.eliminarAmbiente(idAmbiente, confirmacion);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public EstacionResumenDTO agregarEstacionAAmbiente(
            @Argument Long idAmbiente,
            @Argument EstacionInputDTO input) {
        log.info("GraphQL Mutation: agregarEstacionAAmbiente({}, estacion={})", idAmbiente, input.getNombre());
        return coordinadorService.agregarEstacionAAmbiente(idAmbiente, input);
    }
}
