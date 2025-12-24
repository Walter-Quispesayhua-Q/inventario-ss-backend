package com.upeu.gestioninventario.estructuras.mapper;

import com.upeu.gestioninventario.estructuras.dto.estacion.BienEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.ComponenteEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.ComponenteEstacionInputDTO;
import com.upeu.gestioninventario.estructuras.model.BienEstacion;
import com.upeu.gestioninventario.estructuras.model.ComponenteEstacion;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", uses = {BienEstacionMapper.class})
public interface ComponenteEstacionMapper {

    @Mappings({
            @Mapping(source = "idComponente", target = "id"),
            @Mapping(source = "estacion.idEstacion", target = "idEstacionPadre"),
            @Mapping(source = "estacion.nombre", target = "nombreEstacion"),
            @Mapping(source = "estacion.codigo", target = "codigoEstacion"),
            @Mapping(source = "bienesAsignados", target = "bienes", qualifiedByName = "mapBienesActivos"),
            @Mapping(source = "usuarioCreacion.id", target = "idUsuarioCreacion")
    })
    ComponenteEstacionDTO toDTO(ComponenteEstacion entity);

    List<ComponenteEstacionDTO> toDTOList(List<ComponenteEstacion> entities);

    @Mappings({
            @Mapping(target = "idComponente", ignore = true),
            @Mapping(target = "estacion", ignore = true),
            @Mapping(target = "bienesAsignados", ignore = true),
            @Mapping(target = "usuarioCreacion", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "usuarioUltimaModificacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true),
            @Mapping(target = "fechaEliminacion", ignore = true),
            @Mapping(target = "activo", ignore = true)
    })
    ComponenteEstacion toEntity(ComponenteEstacionInputDTO inputDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "idComponente", ignore = true),
            @Mapping(target = "estacion", ignore = true),
            @Mapping(target = "bienesAsignados", ignore = true),
            @Mapping(target = "usuarioCreacion", ignore = true),
            @Mapping(target = "fechaCreacion", ignore = true),
            @Mapping(target = "usuarioUltimaModificacion", ignore = true),
            @Mapping(target = "fechaUltimaModificacion", ignore = true),
            @Mapping(target = "fechaEliminacion", ignore = true),
            @Mapping(target = "activo", ignore = true)
    })
    void updateEntityFromInput(ComponenteEstacionInputDTO inputDTO, @MappingTarget ComponenteEstacion entity);

    @Named("mapBienesActivos")
    default List<BienEstacionDTO> mapBienes(List<BienEstacion> bienes) {
        if (bienes == null) return null;
        return bienes.stream()
                .filter(be -> be.getFechaDesasignacion() == null)
                .map(this::mapBienEstacion)
                .toList();
    }

    @Mappings({
            @Mapping(source = "bien.id", target = "idBien"),
            @Mapping(source = "bien.nombreBien", target = "nombreBien"),
            @Mapping(source = "bien.caf", target = "codigoBien"),
            @Mapping(source = "bien.numeroSerie", target = "numeroSerieBien"),
            @Mapping(source = "estacion.idEstacion", target = "idEstacion"),
            @Mapping(source = "estacion.nombre", target = "nombreEstacion"),
            @Mapping(source = "estacion.codigo", target = "codigoEstacion"),
            @Mapping(source = "estacion.ambiente.idAmbiente", target = "idAmbiente"),
            @Mapping(source = "estacion.ambiente.nombre", target = "nombreAmbiente"),
            @Mapping(source = "estacion.ambiente.piso.idPiso", target = "idPiso"),
            @Mapping(source = "estacion.ambiente.piso.nombre", target = "nombrePiso"),
            @Mapping(source = "estacion.ambiente.piso.numeroPiso", target = "numeroPiso"),
            @Mapping(source = "estacion.ambiente.piso.edificio.idEdificio", target = "idEdificio"),
            @Mapping(source = "estacion.ambiente.piso.edificio.nombre", target = "nombreEdificio"),
            @Mapping(target = "asignacionActiva", expression = "java(bienEstacion.getFechaDesasignacion() == null)")
    })
    BienEstacionDTO mapBienEstacion(BienEstacion bienEstacion);
}
