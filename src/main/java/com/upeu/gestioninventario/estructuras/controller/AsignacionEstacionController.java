package com.upeu.gestioninventario.estructuras.controller;

import com.upeu.gestioninventario.estructuras.dto.asignacion.*;
import com.upeu.gestioninventario.estructuras.model.Estacion;
import com.upeu.gestioninventario.estructuras.service.IAsignacionEstacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AsignacionEstacionController {

    private final IAsignacionEstacionService asignacionEstacionService;

    @QueryMapping
    public Estacion estacionActualDelBien(@Argument Long idBien) {
        log.info("GraphQL Query: estacionActualDelBien({})", idBien);
        return asignacionEstacionService.obtenerEstacionActual(idBien).orElse(null);
    }

    @QueryMapping
    public DiagnosticoBienDTO diagnosticarEstadoBien(@Argument Long idBien) {
        log.info("GraphQL Query: diagnosticarEstadoBien({})", idBien);
        return asignacionEstacionService.diagnosticarEstadoBien(idBien);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoAsignacionDTO asignarBienAEstacion(
            @Argument Long idBien,
            @Argument Long idEstacion,
            @Argument String posicionRelativa,
            @Argument Boolean forzarReasignacion,
            Authentication authentication) {
        log.info("GraphQL Mutation: asignarBienAEstacion(bien={}, estacion={}, forzar={})", 
                 idBien, idEstacion, forzarReasignacion);
        return asignacionEstacionService.asignarBienAEstacion(
            idBien, idEstacion, posicionRelativa, 
            obtenerIdUsuario(authentication), 
            Boolean.TRUE.equals(forzarReasignacion)
        );
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoAsignacionMasivaDTO asignarBienesMasivos(
            @Argument List<Long> idsBienes,
            @Argument Long idEstacion,
            @Argument Boolean forzarReasignacion,
            Authentication authentication) {
        log.info("GraphQL Mutation: asignarBienesMasivos({} bienes a estacion {}, forzar={})", 
                 idsBienes.size(), idEstacion, forzarReasignacion);
        return asignacionEstacionService.asignarBienesMasivos(
            idsBienes, idEstacion, 
            obtenerIdUsuario(authentication), 
            Boolean.TRUE.equals(forzarReasignacion)
        );
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoDesasignacionDTO desasignarBienDeEstacion(
            @Argument Long idBien,
            Authentication authentication) {
        log.info("GraphQL Mutation: desasignarBienDeEstacion({})", idBien);
        return asignacionEstacionService.desasignarBienDeEstacion(idBien, obtenerIdUsuario(authentication));
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoDesasignacionMasivaDTO desasignarBienesMasivos(
            @Argument List<Long> idsBienes,
            Authentication authentication) {
        log.info("GraphQL Mutation: desasignarBienesMasivos({} bienes)", idsBienes.size());
        return asignacionEstacionService.desasignarBienesMasivos(idsBienes, obtenerIdUsuario(authentication));
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoDesasignacionMasivaDTO desagruparBienesDeEstacion(
            @Argument Long idEstacion,
            @Argument List<Long> idsBienes,
            Authentication authentication) {
        log.info("GraphQL Mutation: desagruparBienesDeEstacion(estacion={}, {} bienes)", 
                 idEstacion, idsBienes.size());
        return asignacionEstacionService.desagruparBienesDeEstacion(
            idEstacion, idsBienes, obtenerIdUsuario(authentication)
        );
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public ResultadoGestionBienesDTO gestionarBienesEstacion(
            @Argument Long idEstacion,
            @Argument List<Long> bienesAAgrupar,
            @Argument List<Long> bienesADesagrupar,
            @Argument Boolean forzarReasignacion,
            Authentication authentication) {
        log.info("GraphQL Mutation: gestionarBienesEstacion(estacion={}, agrupar={}, desagrupar={}, forzar={})", 
                 idEstacion, 
                 bienesAAgrupar != null ? bienesAAgrupar.size() : 0,
                 bienesADesagrupar != null ? bienesADesagrupar.size() : 0,
                 forzarReasignacion);
        return asignacionEstacionService.gestionarBienesEstacion(
            idEstacion, bienesAAgrupar, bienesADesagrupar,
            obtenerIdUsuario(authentication), 
            Boolean.TRUE.equals(forzarReasignacion)
        );
    }

    private Long obtenerIdUsuario(Authentication authentication) {
        // TODO: Extraer ID real del usuario autenticado desde el principal
        return 1L;
    }
}
