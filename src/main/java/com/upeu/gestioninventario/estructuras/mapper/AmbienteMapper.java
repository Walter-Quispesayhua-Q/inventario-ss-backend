package com.upeu.gestioninventario.estructuras.mapper;

import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteInputDTO;
import com.upeu.gestioninventario.estructuras.model.Ambiente;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {TipoEstructuraMapper.class, PisoMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AmbienteMapper {

    @Mapping(source = "piso.idPiso", target = "idPiso")
    @Mapping(source = "piso.nombre", target = "nombrePiso")
    @Mapping(source = "piso.codigo", target = "codigoPiso")
    @Mapping(source = "piso.numeroPiso", target = "numeroPiso")
    @Mapping(source = "piso.edificio.idEdificio", target = "idEdificio")
    @Mapping(source = "piso.edificio.nombre", target = "nombreEdificio")
    @Mapping(source = "responsable.id", target = "idResponsable")
    @Mapping(source = "responsable.codigoUsuario", target = "nombreResponsable")
    @Mapping(source = "departamentoResponsable.id", target = "idDepartamentoResponsable")
    @Mapping(source = "departamentoResponsable.nombreDepartamento", target = "nombreDepartamentoResponsable")
    AmbienteDTO toDTO(Ambiente entity);

    @Mapping(target = "idAmbiente", ignore = true)
    @Mapping(target = "piso", ignore = true)
    @Mapping(target = "tipoEstructura", ignore = true)
    @Mapping(target = "responsable", ignore = true)
    @Mapping(target = "departamentoResponsable", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioUltimaModificacion", ignore = true)
    Ambiente toEntity(AmbienteInputDTO inputDTO);

    @Mapping(target = "idAmbiente", ignore = true)
    @Mapping(target = "piso", ignore = true)
    @Mapping(target = "tipoEstructura", ignore = true)
    @Mapping(target = "responsable", ignore = true)
    @Mapping(target = "departamentoResponsable", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioUltimaModificacion", ignore = true)
    void updateEntityFromInput(AmbienteInputDTO inputDTO, @MappingTarget Ambiente entity);

    List<AmbienteDTO> toDTOList(List<Ambiente> entities);
}
