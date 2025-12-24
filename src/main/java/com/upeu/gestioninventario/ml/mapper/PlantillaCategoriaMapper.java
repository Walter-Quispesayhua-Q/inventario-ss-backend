package com.upeu.gestioninventario.ml.mapper;

import com.upeu.gestioninventario.categorias.dto.PlantillaCategoriaActualizacionDTO;
import com.upeu.gestioninventario.categorias.dto.PlantillaCategoriaCreacionDTO;
import com.upeu.gestioninventario.categorias.dto.PlantillaCategoriaDTO;
import com.upeu.gestioninventario.ml.model.PlantillaCategoria;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {PlantillaAtributoMapper.class})
public interface PlantillaCategoriaMapper {

    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "idPlantillaSeed", target = "idPlantillaSeed"),
        @Mapping(source = "nombrePlantilla", target = "nombrePlantilla"),
        @Mapping(source = "version", target = "version"),
        @Mapping(source = "descripcion", target = "descripcion"),
        @Mapping(source = "categoriaSemantica", target = "categoriaSemantica"),
        @Mapping(source = "jsonScoring", target = "jsonScoring"),
        @Mapping(source = "jsonReconocimiento", target = "jsonReconocimiento"),
        @Mapping(source = "jsonPatrones", target = "jsonPatrones"),
        @Mapping(source = "jsonInteligencia", target = "jsonInteligencia"),
        @Mapping(source = "jsonAprendizaje", target = "jsonAprendizaje"),
        @Mapping(source = "plantillaAtributos", target = "plantillaAtributos"),
        @Mapping(source = "usuarioCreacion.id", target = "idUsuarioCreacion"),
        @Mapping(source = "usuarioUltimaModificacion.id", target = "idUsuarioUltimaModificacion"),
        @Mapping(source = "fechaCreacion", target = "fechaCreacion"),
        @Mapping(source = "fechaUltimaModificacion", target = "fechaUltimaModificacion")
    })
    @Named("toDto")
    PlantillaCategoriaDTO toDto(PlantillaCategoria entity);

    @Mappings({
        @Mapping(source = "idPlantillaSeed", target = "idPlantillaSeed"),
        @Mapping(source = "nombrePlantilla", target = "nombrePlantilla"),
        @Mapping(source = "version", target = "version"),
        @Mapping(source = "descripcion", target = "descripcion"),
        @Mapping(source = "categoriaSemantica", target = "categoriaSemantica"),
        @Mapping(source = "jsonScoring", target = "jsonScoring"),
        @Mapping(source = "jsonReconocimiento", target = "jsonReconocimiento"),
        @Mapping(source = "jsonPatrones", target = "jsonPatrones"),
        @Mapping(source = "jsonInteligencia", target = "jsonInteligencia"),
        @Mapping(source = "jsonAprendizaje", target = "jsonAprendizaje"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "plantillaAtributos", ignore = true),
        @Mapping(target = "usuarioCreacion", ignore = true),
        @Mapping(target = "usuarioUltimaModificacion", ignore = true),
        @Mapping(target = "fechaCreacion", ignore = true),
        @Mapping(target = "fechaUltimaModificacion", ignore = true),
        @Mapping(target = "fechaEliminacion", ignore = true)
    })
    PlantillaCategoria toEntity(PlantillaCategoriaCreacionDTO dto);

    @Mappings({
        @Mapping(source = "nombrePlantilla", target = "nombrePlantilla"),
        @Mapping(source = "version", target = "version"),
        @Mapping(source = "descripcion", target = "descripcion"),
        @Mapping(source = "categoriaSemantica", target = "categoriaSemantica"),
        @Mapping(source = "jsonScoring", target = "jsonScoring"),
        @Mapping(source = "jsonReconocimiento", target = "jsonReconocimiento"),
        @Mapping(source = "jsonPatrones", target = "jsonPatrones"),
        @Mapping(source = "jsonInteligencia", target = "jsonInteligencia"),
        @Mapping(source = "jsonAprendizaje", target = "jsonAprendizaje"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "idPlantillaSeed", ignore = true),
        @Mapping(target = "plantillaAtributos", ignore = true),
        @Mapping(target = "usuarioCreacion", ignore = true),
        @Mapping(target = "usuarioUltimaModificacion", ignore = true),
        @Mapping(target = "fechaCreacion", ignore = true),
        @Mapping(target = "fechaUltimaModificacion", ignore = true),
        @Mapping(target = "fechaEliminacion", ignore = true)
    })
    void updateEntityFromDto(PlantillaCategoriaActualizacionDTO dto, @MappingTarget PlantillaCategoria entity);
}