package com.upeu.gestioninventario.inventario.mapper;

import com.upeu.gestioninventario.inventario.dto.bien.AtributoDisplayDTO;
import com.upeu.gestioninventario.inventario.model.BienAtributoValor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AtributoDisplayMapper {

    @Mappings({
            @Mapping(source = "tipoAtributo.id", target = "tipoAtributoId"),
            @Mapping(source = "tipoAtributo.nombreAtributo", target = "nombreAtributo"),
            @Mapping(source = "tipoAtributo.tipoDato", target = "tipoDato"),
            @Mapping(source = "valor", target = "valor")
    })
    AtributoDisplayDTO toDto(BienAtributoValor bienAtributoValor);
}