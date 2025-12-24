package com.upeu.gestioninventario.ml.mapper;

import com.upeu.gestioninventario.inventario.dto.AtributoFormularioDTO;
import com.upeu.gestioninventario.categorias.dto.PlantillaAtributoDTO;
import com.upeu.gestioninventario.categorias.dto.PlantillaAtributoCreacionDTO;
import com.upeu.gestioninventario.ml.model.PlantillaAtributo;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface PlantillaAtributoMapper {

    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "plantilla.id", target = "idPlantilla"),
        @Mapping(source = "tipoAtributo.id", target = "idTipoAtributo"),
        @Mapping(source = "etiqueta", target = "etiqueta"),
        @Mapping(source = "esRequerido", target = "esRequerido"),
        @Mapping(source = "ordenUI", target = "ordenUI"),
        @Mapping(source = "jsonExtraccion", target = "jsonExtraccion"),
        @Mapping(source = "tipoAtributo.nombreAtributo", target = "nombreAtributo"),
        @Mapping(source = "tipoAtributo.tipoDato", target = "tipoDato")
    })
    PlantillaAtributoDTO toDto(PlantillaAtributo entity);

    @Mappings({
        @Mapping(source = "idPlantilla", target = "plantilla.id"),
        @Mapping(source = "idTipoAtributo", target = "tipoAtributo.id"),
        @Mapping(source = "etiqueta", target = "etiqueta"),
        @Mapping(source = "esRequerido", target = "esRequerido"),
        @Mapping(source = "ordenUI", target = "ordenUI"),
        @Mapping(source = "jsonExtraccion", target = "jsonExtraccion"),
        @Mapping(target = "id", ignore = true)
    })
    PlantillaAtributo toEntity(PlantillaAtributoCreacionDTO dto);

    Set<PlantillaAtributoDTO> toDtoSet(Set<PlantillaAtributo> entities);

    @Mappings({
        @Mapping(source = "etiqueta", target = "etiqueta"),
        @Mapping(source = "esRequerido", target = "esRequerido"),
        @Mapping(source = "ordenUI", target = "ordenUI"),
        @Mapping(source = "jsonExtraccion", target = "jsonExtraccion"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "plantilla", ignore = true),
        @Mapping(target = "tipoAtributo", ignore = true)
    })
    void updateEntityFromDto(PlantillaAtributoCreacionDTO dto, @MappingTarget PlantillaAtributo entity);

    @Mappings({
        @Mapping(source = "tipoAtributo.id", target = "tipoAtributoId"),
        @Mapping(source = "tipoAtributo.nombreAtributo", target = "nombreTipoAtributo"),
        @Mapping(source = "tipoAtributo.tipoDato", target = "tipoDato"),
        @Mapping(source = "etiqueta", target = "etiqueta"),
        @Mapping(source = "esRequerido", target = "requerido"),
        @Mapping(source = "ordenUI", target = "orden"),
        @Mapping(source = "placeholder", target = "placeholder")
    })
    AtributoFormularioDTO toAtributoFormularioDTO(PlantillaAtributo plantillaAtributo);
}
