package com.upeu.gestioninventario.estructuras.controller;

import com.upeu.gestioninventario.estructuras.dto.ConfirmacionOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroPisoDTO;
import com.upeu.gestioninventario.estructuras.dto.ResultadoOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteInputDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoConAmbientesResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoInputDTO;
import com.upeu.gestioninventario.estructuras.service.IAmbienteService;
import com.upeu.gestioninventario.estructuras.service.IEdificioService;
import com.upeu.gestioninventario.estructuras.service.IEstructuraCoordinadorService;
import com.upeu.gestioninventario.estructuras.service.IPisoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PisoController {

    private final IPisoService pisoService;
    private final IAmbienteService ambienteService;
    private final IEdificioService edificioService;
    private final IEstructuraCoordinadorService coordinadorService;


    @QueryMapping
    public List<PisoDTO> pisos() {
        log.info("GraphQL Query: pisos");
        return pisoService.listarTodosPisos();
    }

    @QueryMapping
    public PisoConAmbientesResponseDTO pisoConAmbientes(@Argument Long idPiso) {
        log.debug("GraphQL Query: pisoConAmbientes({})", idPiso);
        return coordinadorService.obtenerPisoConAmbientes(idPiso);
    }

    @QueryMapping
    public PisoDTO pisoPorEdificioYNumero(
            @Argument Long idEdificio,
            @Argument Integer numeroPiso) {
        log.info("GraphQL Query: pisoPorEdificioYNumero({}, {})", idEdificio, numeroPiso);
        return pisoService.obtenerPisoPorEdificioYNumero(idEdificio, numeroPiso);
    }

    @QueryMapping
    public List<PisoDTO> buscarPisos(@Argument FiltroPisoDTO filtro) {
        log.info("GraphQL Query: buscarPisos({})", filtro);
        return pisoService.buscarPisosConFiltros(filtro);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public PisoConAmbientesResponseDTO actualizarPiso(
            @Argument Long idPiso,
            @Argument PisoInputDTO input) {
        log.info("GraphQL Mutation: actualizarPiso({})", idPiso);
        return coordinadorService.actualizarPisoConAmbientes(idPiso, input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoOperacionDTO eliminarPiso(
            @Argument Long idEdificio,
            @Argument Long idPiso,
            @Argument ConfirmacionOperacionDTO confirmacion) {
        log.info("GraphQL Mutation: eliminarPiso({}/{}, confirmado={})", 
                 idEdificio, idPiso, confirmacion != null && confirmacion.confirmado());
        return coordinadorService.eliminarPiso(idEdificio, idPiso, confirmacion);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public AmbienteResumenDTO agregarAmbienteAPiso(
            @Argument Long idPiso,
            @Argument AmbienteInputDTO input) {
        log.info("GraphQL Mutation: agregarAmbienteAPiso({}, ambiente={})", idPiso, input.getNombre());
        return coordinadorService.agregarAmbienteAPiso(idPiso, input);
    }

    @SchemaMapping(typeName = "Piso", field = "edificio")
    public EdificioDTO edificio(PisoDTO piso) {
        log.debug("Field Resolver: Piso.edificio(idEdificio={})", piso.getIdEdificio());
        if (piso.getIdEdificio() != null) {
            return edificioService.obtenerEdificioPorId(piso.getIdEdificio());
        }
        return null;
    }

    @SchemaMapping(typeName = "Piso", field = "ambientes")
    public List<AmbienteDTO> ambientes(PisoDTO piso) {
        log.debug("Field Resolver: Piso.ambientes(idPiso={})", piso.getIdPiso());
        return ambienteService.listarAmbientesPorPiso(piso.getIdPiso());
    }

    @SchemaMapping(typeName = "Piso", field = "cantidadAmbientes")
    public Integer cantidadAmbientes(PisoDTO piso) {
        log.debug("Field Resolver: Piso.cantidadAmbientes(idPiso={})", piso.getIdPiso());
        List<AmbienteDTO> ambientes = ambienteService.listarAmbientesPorPiso(piso.getIdPiso());
        return ambientes.size();
    }
}
