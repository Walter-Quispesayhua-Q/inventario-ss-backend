package com.upeu.gestioninventario.estructuras.controller;

import com.upeu.gestioninventario.estructuras.dto.BienesParaEstacionResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.ConfirmacionOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.ResultadoOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.*;
import com.upeu.gestioninventario.estructuras.model.OrdenEstacion;
import com.upeu.gestioninventario.estructuras.service.IEstacionService;
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
public class EstacionController {

    private final IEstacionService estacionService;
    private final IEstructuraCoordinadorService coordinadorService;

    @QueryMapping
    public List<EstacionDTO> estaciones() {
        log.info("GraphQL Query: estaciones");
        return estacionService.listarTodasEstaciones();
    }

    @QueryMapping
    public List<EstacionDTO> estacionesConCapacidad() {
        log.info("GraphQL Query: estacionesConCapacidad");
        return estacionService.obtenerEstacionesConCapacidadDisponible();
    }

    @QueryMapping
    public List<EstacionDTO> buscarEstaciones(
            @Argument FiltroEstacionDTO filtro,
            @Argument OrdenEstacion orden) {
        log.info("GraphQL Query: buscarEstaciones({}, {})", filtro, orden);
        return estacionService.buscarEstacionesConFiltros(filtro, orden);
    }

    @QueryMapping
    public EstacionConBienesResponseDTO estacionConBienes(@Argument Long idEstacion) {
        log.info("GraphQL Query: estacionConBienes({})", idEstacion);
        return coordinadorService.obtenerEstacion(idEstacion);
    }

    @QueryMapping
    public BienEstacionPaginadoDTO bienesDeEstacion(
            @Argument Long idEstacion,
            @Argument Integer pagina,
            @Argument Integer tamanoPagina) {
        log.info("GraphQL Query: bienesDeEstacion({}, {}, {})", idEstacion, pagina, tamanoPagina);
        return estacionService.obtenerBienesDeEstacionPaginados(idEstacion, pagina, tamanoPagina);
    }

    @QueryMapping
    public BienesParaEstacionResponseDTO bienesParaEstacion(@Argument Long idEstacion) {
        log.info("GraphQL Query: bienesParaEstacion({})", idEstacion);
        return estacionService.obtenerBienesParaFormulario(idEstacion);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public EstacionResumenDTO actualizarEstacion(
            @Argument Long idEstacion,
            @Argument EstacionInputDTO input,
            @Argument ConfirmacionOperacionDTO confirmacion) {
        log.info("GraphQL Mutation: actualizarEstacion({}, confirmado={})", 
                 idEstacion, confirmacion != null && confirmacion.confirmado());
        return coordinadorService.actualizarEstacion(idEstacion, input, confirmacion);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoOperacionDTO eliminarEstacion(
            @Argument Long idEstacion,
            @Argument ConfirmacionOperacionDTO confirmacion) {
        log.info("GraphQL Mutation: eliminarEstacion({}, confirmado={})", 
                 idEstacion, confirmacion != null && confirmacion.confirmado());
        return coordinadorService.eliminarEstacion(idEstacion, confirmacion);
    }
}
