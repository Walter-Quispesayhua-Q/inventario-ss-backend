package com.upeu.gestioninventario.estructuras.mapper;

import com.upeu.gestioninventario.estructuras.dto.piso.PisoDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoInputDTO;
import com.upeu.gestioninventario.estructuras.model.Piso;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {TipoEstructuraMapper.class, EdificioMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PisoMapper {

    @Mapping(source = "edificio.idEdificio", target = "idEdificio")
    @Mapping(source = "edificio.nombre", target = "nombreEdificio")
    @Mapping(source = "edificio.codigo", target = "codigoEdificio")
    @Mapping(source = "responsableMantenimiento.id", target = "idResponsableMantenimiento")
    @Mapping(source = "responsableMantenimiento.codigoUsuario", target = "nombreResponsableMantenimiento")
    PisoDTO toDTO(Piso entity);

    @Mapping(target = "idPiso", ignore = true)
    @Mapping(target = "edificio", ignore = true)
    @Mapping(target = "tipoEstructura", ignore = true)
    @Mapping(target = "responsableMantenimiento", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioUltimaModificacion", ignore = true)
    Piso toEntity(PisoInputDTO inputDTO);

    @Mapping(target = "idPiso", ignore = true)
    @Mapping(target = "edificio", ignore = true)
    @Mapping(target = "tipoEstructura", ignore = true)
    @Mapping(target = "responsableMantenimiento", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaUltimaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioUltimaModificacion", ignore = true)
    void updateEntityFromInput(PisoInputDTO inputDTO, @MappingTarget Piso entity);

    List<PisoDTO> toDTOList(List<Piso> entities);
}
