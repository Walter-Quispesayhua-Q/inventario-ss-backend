package com.upeu.gestioninventario.inventario.mapper;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.inventario.historial.dto.HistorialUbicacionDTO;
import com.upeu.gestioninventario.inventario.historial.model.HistorialUbicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface HistorialUbicacionMapper {

    @Mappings({
            @Mapping(source = "bien.id", target = "bienId"),
            @Mapping(source = "bien.nombreBien", target = "bienNombre"),
            @Mapping(source = "bien.caf", target = "bienCaf"),
            @Mapping(source = "ubicacion.id", target = "ubicacionId"),
            @Mapping(source = "ubicacion.nombreUbicacion", target = "ubicacionNombre"),
            @Mapping(source = "usuarioRegistro.id", target = "usuarioRegistroId"),
            @Mapping(source = "usuarioRegistro", target = "usuarioRegistroNombre", qualifiedByName = "usuarioToFullName")
    })
    HistorialUbicacionDTO toDto(HistorialUbicacion historialUbicacion);

    @Named("usuarioToFullName")
    default String usuarioToFullName(Usuario usuario) {
        if (usuario == null || usuario.getPersona() == null) {
            return null;
        }
        return usuario.getPersona().getNombre() + " " + usuario.getPersona().getApellido();
    }
}