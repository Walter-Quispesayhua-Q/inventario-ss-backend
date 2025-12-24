package com.upeu.gestioninventario.inventario.mapper;

import com.upeu.gestioninventario.inventario.dto.bien.BienAtributoValorDTO;
import com.upeu.gestioninventario.inventario.model.BienAtributoValor;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BienAtributoValorMapper {

    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "bien.id", target = "bienId"),
        @Mapping(source = "tipoAtributo.id", target = "tipoAtributoId"),
        @Mapping(source = "tipoAtributo.nombreAtributo", target = "nombreAtributo"),
        @Mapping(source = "tipoAtributo.tipoDato", target = "tipoDato"),
        @Mapping(source = "valor", target = "valor"),
        @Mapping(source = "fechaCreacion", target = "fechaCreacion"),
        @Mapping(source = "fechaUltimaModificacion", target = "fechaUltimaModificacion"),
        @Mapping(target = "etiqueta", ignore = true)
    })
    BienAtributoValorDTO toDto(BienAtributoValor entity);

    @Mappings({
        @Mapping(source = "bienId", target = "bien.id"),
        @Mapping(source = "tipoAtributoId", target = "tipoAtributo.id"),
        @Mapping(source = "valor", target = "valor"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "fechaCreacion", ignore = true),
        @Mapping(target = "fechaUltimaModificacion", ignore = true)
    })
    BienAtributoValor toEntity(BienAtributoValorDTO dto);


    void updateEntityFromDto(BienAtributoValorDTO dto, @MappingTarget BienAtributoValor entity);

}
