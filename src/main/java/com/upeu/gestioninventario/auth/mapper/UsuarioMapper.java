package com.upeu.gestioninventario.auth.mapper;

import com.upeu.gestioninventario.auth.dto.UsuarioCreacionInput;
import com.upeu.gestioninventario.auth.dto.UsuarioDTO;
import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.model.UsuarioRol;
import com.upeu.gestioninventario.personas.dto.PersonaDTO;
import com.upeu.gestioninventario.personas.model.Persona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(source = "departamento.nombreDepartamento", target = "departamentoNombre")
    @Mapping(source = "usuarioRoles", target = "roles", qualifiedByName = "mapRoles")
    @Mapping(source = "persona", target = "persona", qualifiedByName = "mapPersonaSimple")
    UsuarioDTO toDto(Usuario usuario);

    @Named("mapRoles")
    default List<String> mapRoles(java.util.Set<UsuarioRol> usuarioRoles) {
        if (usuarioRoles == null || usuarioRoles.isEmpty()) {
            return Collections.emptyList();
        }
        return usuarioRoles.stream()
                .map(ur -> ur.getRol().getNombreRol())
                .toList();
    }

    @Named("mapPersonaSimple")
    default PersonaDTO mapPersonaSimple(Persona p) {
        if (p == null) return null;
        return new PersonaDTO(
                p.getId(),
                p.getNombre(),
                p.getApellido(),
                p.getIdentificacion(),
                p.getCodigo(),
                p.getEmail(),
                p.getTelefono(),
                p.getFechaCreacion(),
                p.getFechaUltimaModificacion(),
                null, null, null
        );
    }

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "email", target = "email"),
            @Mapping(target = "passwordHash", ignore = true),
            @Mapping(target = "codigoUsuario", ignore = true),
            @Mapping(target = "activo", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true),
            @Mapping(target = "ultimoLogin", ignore = true),
            @Mapping(target = "fechaEliminacion", ignore = true),
            @Mapping(target = "persona", ignore = true),
            @Mapping(target = "departamento", ignore = true)
    })
    Usuario toEntity(UsuarioCreacionInput usuarioCreacionInput);
}