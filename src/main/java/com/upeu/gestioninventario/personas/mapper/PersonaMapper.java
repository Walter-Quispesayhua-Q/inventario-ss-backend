package com.upeu.gestioninventario.personas.mapper;

import com.upeu.gestioninventario.auth.dto.UsuarioCreacionInput;
import com.upeu.gestioninventario.personas.dto.PersonaActualizacionDTO;
import com.upeu.gestioninventario.personas.dto.PersonaCreacionDTO;
import com.upeu.gestioninventario.personas.dto.PersonaDTO;
import com.upeu.gestioninventario.personas.model.Persona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PersonaMapper {

    PersonaDTO toDto(Persona persona);
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true),
            @Mapping(target = "fechaEliminacion", ignore = true)
    })
    Persona toEntity(PersonaCreacionDTO creacionDTO);
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "identificacion", ignore = true),
            @Mapping(target = "codigo", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true),
            @Mapping(target = "fechaEliminacion", ignore = true)
    })
    void updateEntityFromDto(PersonaActualizacionDTO dto, @MappingTarget Persona entity);


    @Mappings({
            @Mapping(source = "firstName", target = "nombre"),
            @Mapping(source = "lastName", target = "apellido"),
            @Mapping(source = "email", target = "email"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "identificacion", ignore = true),
            @Mapping(target = "codigo", ignore = true),
            @Mapping(target = "telefono", ignore = true),
            @Mapping(target = "rol", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true),
            @Mapping(target = "fechaEliminacion", ignore = true)
    })
    Persona toEntityFromUsuarioCreacion(UsuarioCreacionInput dto);
}