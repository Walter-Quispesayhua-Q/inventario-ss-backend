package com.upeu.gestioninventario.categorias.mapper;

import com.upeu.gestioninventario.categorias.dto.CategoriaAtributoDTO;
import com.upeu.gestioninventario.categorias.model.CategoriaAtributo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {TipoAtributoMapper.class})
public interface CategoriaAtributoMapper {

    @Mappings({
            @Mapping(source = "categoria.nombreCategoria", target = "nombreCategoria"),
            @Mapping(source = "categoria.descripcion", target = "descripcion"),
            @Mapping(source = "tipoAtributo", target = "tipoAtributo"),
            @Mapping(source = "requerido", target = "activo")
    })
    CategoriaAtributoDTO toDto(CategoriaAtributo categoriaAtributo);
}
