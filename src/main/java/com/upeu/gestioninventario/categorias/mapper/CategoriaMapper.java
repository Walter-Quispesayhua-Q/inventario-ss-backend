package com.upeu.gestioninventario.categorias.mapper;

import com.upeu.gestioninventario.categorias.dto.CategoriaActualizacionDTO;
import com.upeu.gestioninventario.categorias.dto.CategoriaCreacionDTO;
import com.upeu.gestioninventario.categorias.dto.CategoriaDTO;
import com.upeu.gestioninventario.categorias.dto.CategoriaSimpleDTO;
import com.upeu.gestioninventario.categorias.model.Categoria;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    @Mappings({
            @Mapping(source = "usuarioCreacion.persona.nombre", target = "usuarioCreacionNombre"),
            @Mapping(source = "usuarioUltimaModificacion.persona.nombre", target = "usuarioUltimaModificacionNombre")
    })
    CategoriaDTO toDto(Categoria categoria);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "usuarioCreacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true),
            @Mapping(target = "usuarioUltimaModificacion", ignore = true),
            @Mapping(target = "fechaEliminacion", ignore = true)
    })
    Categoria toEntity(CategoriaCreacionDTO creacionDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CategoriaActualizacionDTO dto, @MappingTarget Categoria entity);


    CategoriaSimpleDTO toSimpleDto(Categoria categoria);
}