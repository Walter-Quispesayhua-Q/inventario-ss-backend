package com.upeu.gestioninventario.auth.mapper;

import com.upeu.gestioninventario.auth.dto.PermisoDTO;
import com.upeu.gestioninventario.auth.model.Permiso;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermisoMapper {
    PermisoDTO toDto(Permiso permiso);
}