package com.upeu.gestioninventario.auditoria.controller;

import com.upeu.gestioninventario.auditoria.dto.RegistroActividadCreacionInput;
import com.upeu.gestioninventario.auditoria.dto.RegistroActividadDTO;
import com.upeu.gestioninventario.auditoria.dto.RegistrosActividadPaginados;
import com.upeu.gestioninventario.auditoria.service.IAuditoriaService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AuditoriaController {

    private final IAuditoriaService auditoriaService;

    @MutationMapping
    public OperacionResultadoDTO<RegistroActividadDTO> crearRegistroActividad(
            @Argument("registro") @Valid RegistroActividadCreacionInput input) {
        log.info("GraphQL Mutation: crearRegistroActividad");
        return auditoriaService.crearRegistroManual(input);
    }

    @QueryMapping
    public RegistrosActividadPaginados obtenerRegistrosActividad(
            @Argument Integer page,
            @Argument Integer limit,
            @Argument String buscar,
            @Argument Long usuarioId,
            @Argument String tipoOperacion,
            @Argument String entidadAfectada,
            @Argument Long entidadId,
            @Argument String fechaInicio,
            @Argument String fechaFin,
            @Argument String ordenarPor,
            @Argument String orden) {
        log.debug("GraphQL Query: obtenerRegistrosActividad(page={}, limit={})", page, limit);

        OffsetDateTime fechaInicioDateTime = parseToOffsetDateTime(fechaInicio);
        OffsetDateTime fechaFinDateTime = parseToOffsetDateTime(fechaFin);

        return auditoriaService.obtenerRegistrosPaginados(
                page, limit, buscar, usuarioId, tipoOperacion,
                entidadAfectada, entidadId, fechaInicioDateTime,
                fechaFinDateTime, ordenarPor, orden
        );
    }

    @QueryMapping
    public RegistroActividadDTO registroActividadPorId(@Argument Long id) {
        log.debug("GraphQL Query: registroActividadPorId({})", id);
        return auditoriaService.obtenerRegistroPorId(id);
    }

    @QueryMapping
    public List<RegistroActividadDTO> actividadesPorUsuario(@Argument Long usuarioId) {
        log.debug("GraphQL Query: actividadesPorUsuario({})", usuarioId);
        return auditoriaService.obtenerActividadesPorUsuario(usuarioId);
    }

    @QueryMapping
    public List<RegistroActividadDTO> actividadesPorEntidad(
            @Argument String entidadAfectada,
            @Argument Long entidadId) {
        log.debug("GraphQL Query: actividadesPorEntidad({}, {})", entidadAfectada, entidadId);
        return auditoriaService.obtenerActividadesPorEntidad(entidadAfectada, entidadId);
    }

    private OffsetDateTime parseToOffsetDateTime(String s) {
        if (s == null) return null;
        try {
            return OffsetDateTime.parse(s);
        } catch (DateTimeParseException ex) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(s);
                return ldt.atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException ex2) {
                log.warn("No se pudo parsear la fecha: {}", s);
                return null;
            }
        }
    }
}
