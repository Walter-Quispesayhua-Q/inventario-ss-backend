package com.upeu.gestioninventario.categorias.mapper;

import com.upeu.gestioninventario.categorias.dto.tipoatributo.TipoAtributoCreacionDTO;
import com.upeu.gestioninventario.categorias.dto.tipoatributo.TipoAtributoDTO;
import com.upeu.gestioninventario.categorias.model.TipoAtributo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TipoAtributoMapper {

    TipoAtributoDTO toDto(TipoAtributo tipoAtributo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(source = "nombreTipoAtributo", target = "nombreAtributo")
    TipoAtributo toEntity(TipoAtributoCreacionDTO creacionDTO);
}