package com.upeu.gestioninventario.configuracion.mapper;

import com.upeu.gestioninventario.configuracion.dto.ParametroSistemaActualizacionDTO;
import com.upeu.gestioninventario.configuracion.dto.ParametroSistemaCreacionDTO;
import com.upeu.gestioninventario.configuracion.dto.ParametroSistemaDTO;
import com.upeu.gestioninventario.configuracion.model.ParametroSistema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ParametroSistemaMapper {
    @Mapping(target = "id", ignore = true)
    ParametroSistemaDTO toDto(ParametroSistema parametroSistema);

    @Mappings({
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true)
    })
    ParametroSistema toEntity(ParametroSistemaCreacionDTO creacionDTO);

    @Mappings({
            @Mapping(target = "clave", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true)
    })
    void updateEntityFromDto(ParametroSistemaActualizacionDTO dto, @MappingTarget ParametroSistema entity);
}