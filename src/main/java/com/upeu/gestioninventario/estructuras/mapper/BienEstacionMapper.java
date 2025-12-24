package com.upeu.gestioninventario.estructuras.mapper;

import com.upeu.gestioninventario.estructuras.dto.estacion.BienEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.BienEstacionInputDTO;
import com.upeu.gestioninventario.estructuras.model.BienEstacion;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {EstacionMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BienEstacionMapper {

    @Named("toDTO")
    @Mapping(source = "bien.id", target = "idBien")
    @Mapping(source = "bien.nombreBien", target = "nombreBien")
    @Mapping(source = "bien.caf", target = "codigoBien")
    @Mapping(source = "bien.numeroSerie", target = "numeroSerieBien")
    @Mapping(source = "estacion.idEstacion", target = "idEstacion")
    @Mapping(source = "estacion.nombre", target = "nombreEstacion")
    @Mapping(source = "estacion.codigo", target = "codigoEstacion")
    @Mapping(source = "estacion.ambiente.idAmbiente", target = "idAmbiente")
    @Mapping(source = "estacion.ambiente.nombre", target = "nombreAmbiente")
    @Mapping(source = "estacion.ambiente.piso.idPiso", target = "idPiso")
    @Mapping(source = "estacion.ambiente.piso.nombre", target = "nombrePiso")
    @Mapping(source = "estacion.ambiente.piso.numeroPiso", target = "numeroPiso")
    @Mapping(source = "estacion.ambiente.piso.edificio.idEdificio", target = "idEdificio")
    @Mapping(source = "estacion.ambiente.piso.edificio.nombre", target = "nombreEdificio")
    @Mapping(target = "asignacionActiva", expression = "java(entity.getFechaDesasignacion() == null)")
    BienEstacionDTO toDTO(BienEstacion entity);

    @Named("toDTOSimple")
    @Mapping(source = "bien.id", target = "idBien")
    @Mapping(source = "bien.nombreBien", target = "nombreBien")
    @Mapping(source = "estacion.idEstacion", target = "idEstacion")
    @Mapping(source = "estacion.nombre", target = "nombreEstacion")
    @Mapping(target = "asignacionActiva", expression = "java(entity.getFechaDesasignacion() == null)")
    BienEstacionDTO toDTOSimple(BienEstacion entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bien", ignore = true)
    @Mapping(target = "estacion", ignore = true)
    @Mapping(target = "fechaAsignacion", ignore = true)
    @Mapping(target = "fechaDesasignacion", ignore = true)
    @Mapping(target = "idUsuarioAsignacion", ignore = true)
    BienEstacion toEntity(BienEstacionInputDTO inputDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bien", ignore = true)
    @Mapping(target = "estacion", ignore = true)
    @Mapping(target = "fechaAsignacion", ignore = true)
    @Mapping(target = "fechaDesasignacion", ignore = true)
    @Mapping(target = "idUsuarioAsignacion", ignore = true)
    void updateEntityFromInput(BienEstacionInputDTO inputDTO, @MappingTarget BienEstacion entity);

    @IterableMapping(qualifiedByName = "toDTO")
    List<BienEstacionDTO> toDTOList(List<BienEstacion> entities);

}
