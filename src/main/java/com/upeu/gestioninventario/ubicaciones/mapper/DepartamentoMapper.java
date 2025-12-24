package com.upeu.gestioninventario.ubicaciones.mapper;

import com.upeu.gestioninventario.ubicaciones.dto.DepartamentoActualizacionDTO;
import com.upeu.gestioninventario.ubicaciones.dto.DepartamentoCreacionDTO;
import com.upeu.gestioninventario.ubicaciones.dto.DepartamentoDTO;
import com.upeu.gestioninventario.ubicaciones.dto.DepartamentoSimpleDTO;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface DepartamentoMapper {

    DepartamentoSimpleDTO toSimpleDto(Departamento departamento);

    DepartamentoDTO toDto(Departamento departamento);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true)
    })
    Departamento toEntity(DepartamentoCreacionDTO creacionDTO);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true)
    })
    void updateEntityFromDto(DepartamentoActualizacionDTO dto, @MappingTarget Departamento entity);
}