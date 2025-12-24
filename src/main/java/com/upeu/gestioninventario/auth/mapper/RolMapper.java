package com.upeu.gestioninventario.auth.mapper;

import com.upeu.gestioninventario.auth.dto.RolCreacionDTO;
import com.upeu.gestioninventario.auth.dto.RolDTO;
import com.upeu.gestioninventario.auth.model.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RolMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Rol toEntity(RolCreacionDTO rolCreacionDTO);

    RolDTO toDto(Rol rol);
}