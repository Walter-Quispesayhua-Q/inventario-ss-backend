package com.upeu.gestioninventario.auditoria.mapper;

import com.upeu.gestioninventario.auditoria.dto.RegistroActividadCreacionInput;
import com.upeu.gestioninventario.auditoria.dto.RegistroActividadDTO;
import com.upeu.gestioninventario.auditoria.model.RegistroActividad;
import com.upeu.gestioninventario.auditoria.model.TipoEndpoint;
import com.upeu.gestioninventario.auditoria.model.TipoOperacion;
import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.inventario.model.Bien;
import jakarta.persistence.EntityManager;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RegistroActividadMapper {

    @Mappings({
            @Mapping(source = "tipoOperacion", target = "tipoOperacion", qualifiedByName = "tipoOperacionToString"),
            @Mapping(source = "tipoEndpoint", target = "tipoEndpoint", qualifiedByName = "tipoEndpointToString"),
            @Mapping(target = "entidadNombre", source = "entity", qualifiedByName = "mapEntidadNombre"),
            @Mapping(source = "usuario.id", target = "usuarioId"),
            @Mapping(source = "usuario", target = "usuarioNombre", qualifiedByName = "mapUsuarioNombre"),
            @Mapping(source = "usuario.email", target = "usuarioEmail")
    })
    RegistroActividadDTO toDto(RegistroActividad entity, @Context EntityManager em);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "tipoOperacion", target = "tipoOperacion", qualifiedByName = "stringToTipoOperacion"),
            @Mapping(source = "tipoEndpoint", target = "tipoEndpoint", qualifiedByName = "stringToTipoEndpoint"),
            @Mapping(target = "fechaOperacion", ignore = true),
            @Mapping(target = "usuario", ignore = true),
            @Mapping(target = "usuarioCreacion", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "usuarioUltimaModificacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true),
            @Mapping(target = "fechaEliminacion", ignore = true)
    })
    RegistroActividad toEntity(RegistroActividadCreacionInput input);

    @Named("mapUsuarioNombre")
    default String mapUsuarioNombre(Usuario usuario) {
        if (usuario == null) {
            return "Sistema";
        }
        if (usuario.getPersona() == null) {
            return usuario.getEmail();
        }
        String nombre = usuario.getPersona().getNombre();
        String apellido = usuario.getPersona().getApellido();
        return nombre + " " + apellido;
    }

    @Named("tipoOperacionToString")
    default String tipoOperacionToString(TipoOperacion tipoOperacion) {
        return tipoOperacion != null ? tipoOperacion.name() : null;
    }

    @Named("stringToTipoOperacion")
    default TipoOperacion stringToTipoOperacion(String tipoOperacion) {
        return tipoOperacion != null ? TipoOperacion.valueOf(tipoOperacion) : null;
    }

    @Named("stringToTipoEndpoint")
    default TipoEndpoint stringToTipoEndpoint(String tipoEndpoint) {
        return tipoEndpoint != null ? TipoEndpoint.valueOf(tipoEndpoint) : null;
    }

    @Named("tipoEndpointToString")
    default String tipoEndpointToString(TipoEndpoint tipoEndpoint) {
        return tipoEndpoint != null ? tipoEndpoint.name() : null;
    }

    @Named("mapEntidadNombre")
    default String mapEntidadNombre(RegistroActividad entity, @Context EntityManager em) {
        if (entity.getEntidadId() == null || entity.getEntidadAfectada() == null) {
            return null;
        }

        if ("Bien".equalsIgnoreCase(entity.getEntidadAfectada())) {
            try {
                Bien bien = em.getReference(Bien.class, entity.getEntidadId());
                return bien.getNombreBien();
            } catch (jakarta.persistence.EntityNotFoundException e) {
                return "Bien ID " + entity.getEntidadId() + " no encontrado";
            }
        }

        return null;
    }
}
