package com.upeu.gestioninventario.estructuras.mapper;

import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;
import com.upeu.gestioninventario.estructuras.model.TipoEstructuraEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TipoEstructuraMapper {

    TipoEstructuraDTO toDTO(TipoEstructuraEntity entity);

    List<TipoEstructuraDTO> toDTOList(List<TipoEstructuraEntity> entities);
}

