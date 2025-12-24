package com.upeu.gestioninventario.personas.controller;

import com.upeu.gestioninventario.personas.dto.PersonaActualizacionDTO;
import com.upeu.gestioninventario.personas.dto.PersonaConBienesDTO;
import com.upeu.gestioninventario.personas.dto.PersonaCreacionDTO;
import com.upeu.gestioninventario.personas.dto.PersonaDTO;
import com.upeu.gestioninventario.personas.service.IPersonaService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
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
public class PersonaController {

    private final IPersonaService personaService;

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public List<PersonaDTO> personas() {
        log.debug("GraphQL Query: personas");
        return personaService.obtenerTodas();
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public PersonaDTO personaPorId(@Argument Long id) {
        log.debug("GraphQL Query: personaPorId({})", id);
        return personaService.obtenerPorId(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public PersonaDTO personaPorCodigo(@Argument String codigo) {
        log.debug("GraphQL Query: personaPorCodigo({})", codigo);
        return personaService.obtenerPorCodigo(codigo);
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public PersonaConBienesDTO personaConBienes(@Argument Long id) {
        log.debug("GraphQL Query: personaConBienes({})", id);
        return personaService.obtenerConBienes(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public List<PersonaDTO> personasSinUsuario() {
        log.debug("GraphQL Query: personasSinUsuario");
        return personaService.obtenerSinUsuario();
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public OperacionResultadoDTO<PersonaDTO> crearPersona(@Argument("persona") PersonaCreacionDTO persona) {
        log.info("GraphQL Mutation: crearPersona({})", persona.codigo());
        return personaService.crear(persona);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public OperacionResultadoDTO<PersonaDTO> actualizarPersona(
            @Argument Long id,
            @Argument("persona") PersonaActualizacionDTO persona) {
        log.info("GraphQL Mutation: actualizarPersona({})", id);
        return personaService.actualizar(id, persona);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public OperacionResultadoDTO<Void> eliminarPersona(@Argument Long id) {
        log.info("GraphQL Mutation: eliminarPersona({})", id);
        return personaService.eliminar(id);
    }
}
