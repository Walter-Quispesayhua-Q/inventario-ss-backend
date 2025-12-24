package com.upeu.gestioninventario.estructuras.mapper;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionInputDTO;
import com.upeu.gestioninventario.estructuras.model.Estacion;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {TipoEstructuraMapper.class, AmbienteMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EstacionMapper {

    @Mapping(source = "ambiente.idAmbiente", target = "idAmbiente")
    @Mapping(source = "ambiente.nombre", target = "nombreAmbiente")
    @Mapping(source = "ambiente.codigo", target = "codigoAmbiente")
    @Mapping(source = "ambiente.piso.idPiso", target = "idPiso")
    @Mapping(source = "ambiente.piso.nombre", target = "nombrePiso")
    @Mapping(source = "ambiente.piso.numeroPiso", target = "numeroPiso")
    @Mapping(source = "ambiente.piso.edificio.idEdificio", target = "idEdificio")
    @Mapping(source = "ambiente.piso.edificio.nombre", target = "nombreEdificio")
    @Mapping(source = "responsable.id", target = "idResponsable")
    @Mapping(target = "nombreResponsable", expression = "java(mapResponsableNombre(entity))")
    EstacionDTO toDTO(Estacion entity);
    
    default String mapResponsableNombre(Estacion estacion) {
        if (estacion.getResponsable() == null) {
            return null;
        }
        var persona = estacion.getResponsable();
        return persona.getNombre() + " " + persona.getApellido();
    }

    @Mapping(target = "idEstacion", ignore = true)
    @Mapping(target = "ambiente", ignore = true)
    @Mapping(target = "tipoEstructura", ignore = true)
    @Mapping(target = "capacidadBienes", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioUltimaModificacion", ignore = true)
    Estacion toEntity(EstacionInputDTO inputDTO);

    @Mapping(target = "idEstacion", ignore = true)
    @Mapping(target = "ambiente", ignore = true)
    @Mapping(target = "tipoEstructura", ignore = true)
    @Mapping(target = "responsable", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioUltimaModificacion", ignore = true)
    void updateEntityFromInput(EstacionInputDTO inputDTO, @MappingTarget Estacion entity);

    List<EstacionDTO> toDTOList(List<Estacion> entities);
}
