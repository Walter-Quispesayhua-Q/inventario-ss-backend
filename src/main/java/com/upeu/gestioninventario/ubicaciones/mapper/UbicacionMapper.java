package com.upeu.gestioninventario.ubicaciones.mapper;

import com.upeu.gestioninventario.ubicaciones.dto.UbicacionActualizacionInput;
import com.upeu.gestioninventario.ubicaciones.dto.UbicacionDTO;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UbicacionMapper {

    UbicacionDTO toDto(Ubicacion ubicacion);

    void updateEntityFromDto(UbicacionActualizacionInput dto, @MappingTarget Ubicacion entity);
}