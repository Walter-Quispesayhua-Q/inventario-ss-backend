package com.upeu.gestioninventario.estructuras.mapper;

import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioInputDTO;
import com.upeu.gestioninventario.estructuras.model.Edificio;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {TipoEstructuraMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EdificioMapper {

    @Mapping(source = "responsableMantenimiento.id", target = "idResponsableMantenimiento")
    @Mapping(source = "responsableMantenimiento.codigoUsuario", target = "nombreResponsableMantenimiento")
    EdificioDTO toDTO(Edificio entity);

    @Mapping(target = "idEdificio", ignore = true)
    @Mapping(target = "tipoEstructura", ignore = true)
    @Mapping(target = "responsableMantenimiento", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioUltimaModificacion", ignore = true)
    Edificio toEntity(EdificioInputDTO inputDTO);

    @Mapping(target = "idEdificio", ignore = true)
    @Mapping(target = "tipoEstructura", ignore = true)
    @Mapping(target = "responsableMantenimiento", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioUltimaModificacion", ignore = true)
    void updateEntityFromInput(EdificioInputDTO inputDTO, @MappingTarget Edificio entity);

    List<EdificioDTO> toDTOList(List<Edificio> entities);
}
