package com.upeu.gestioninventario.estructuras.mapper;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionCompletaDTO.UbicacionCompletaDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionCompletaDTO.AuditoriaCompletaDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionCompletaDTO.UsuarioDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionCompletaDTO.DepartamentoDTO;
import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;
import com.upeu.gestioninventario.estructuras.model.*;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@Slf4j
public abstract class EstacionCompletaMapper {

    @Autowired
    protected TipoEstructuraMapper tipoEstructuraMapper;

    @Autowired
    protected UsuarioRepository usuarioRepository;


    @Named("mapUbicacion")
    protected UbicacionCompletaDTO mapUbicacion(Estacion estacion) {
        if (estacion == null || estacion.getAmbiente() == null) {
            return null;
        }

        var ambiente = estacion.getAmbiente();
        var piso = ambiente.getPiso();
        var edificio = piso != null ? piso.getEdificio() : null;

        return UbicacionCompletaDTO.builder()
                .ambiente(mapAmbiente(ambiente))
                .piso(mapPiso(piso))
                .edificio(mapEdificio(edificio))
                .build();
    }

    @Named("mapAuditoria")
    protected AuditoriaCompletaDTO mapAuditoria(Estacion estacion) {
        if (estacion == null) {
            return null;
        }

        return AuditoriaCompletaDTO.builder()
                .fechaCreacion(estacion.getFechaCreacion())
                .fechaUltimaModificacion(estacion.getFechaUltimaModificacion())
                .fechaEliminacion(estacion.getFechaEliminacion())
                .usuarioCreacion(mapUsuario(estacion.getUsuarioCreacion()))
                .usuarioUltimaModificacion(mapUsuario(estacion.getUsuarioUltimaModificacion()))
                .build();
    }

    private UbicacionCompletaDTO.AmbienteCompletaDTO mapAmbiente(
            Ambiente ambiente) {
        if (ambiente == null) {
            return null;
        }

        return UbicacionCompletaDTO.AmbienteCompletaDTO.builder()
                .idAmbiente(ambiente.getIdAmbiente())
                .nombre(ambiente.getNombre())
                .codigo(ambiente.getCodigo())
                .capacidadPersonas(ambiente.getCapacidadPersonas())
                .tipoEstructura(mapTipoEstructura(ambiente.getTipoEstructura()))
                .observaciones(ambiente.getObservaciones())
                .propiedadesAdicionales(ambiente.getPropiedadesAdicionales())
                .responsable(mapUsuario(ambiente.getResponsable()))
                .departamentoResponsable(mapDepartamento(ambiente.getDepartamentoResponsable()))
                .build();
    }

    private UbicacionCompletaDTO.PisoCompletaDTO mapPiso(
            Piso piso) {
        if (piso == null) {
            return null;
        }

        return UbicacionCompletaDTO.PisoCompletaDTO.builder()
                .idPiso(piso.getIdPiso())
                .nombre(piso.getNombre())
                .codigo(piso.getCodigo())
                .numeroPiso(piso.getNumeroPiso())
                .tipoEstructura(mapTipoEstructura(piso.getTipoEstructura()))
                .observaciones(piso.getObservaciones())
                .propiedadesAdicionales(piso.getPropiedadesAdicionales())
                .responsableMantenimiento(mapUsuario(piso.getResponsableMantenimiento()))
                .build();
    }

    private UbicacionCompletaDTO.EdificioCompletaDTO mapEdificio(
            Edificio edificio) {
        if (edificio == null) {
            return null;
        }

        return UbicacionCompletaDTO.EdificioCompletaDTO.builder()
                .idEdificio(edificio.getIdEdificio())
                .nombre(edificio.getNombre())
                .codigo(edificio.getCodigo())
                .direccion(edificio.getDireccion())
                .numeroPisos(edificio.getNumeroPisos())
                .areaTotalM2(edificio.getAreaTotalM2())
                .anoConstruccion(edificio.getAnoConstruccion())
                .tipoEstructura(mapTipoEstructura(edificio.getTipoEstructura()))
                .observaciones(edificio.getObservaciones())
                .propiedadesAdicionales(edificio.getPropiedadesAdicionales())
                .responsableMantenimiento(mapUsuario(edificio.getResponsableMantenimiento()))
                .build();
    }

    protected UsuarioDTO mapUsuario(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        String nombre = usuario.getPersona() != null ? usuario.getPersona().getNombre() : "N/A";
        String apellido = usuario.getPersona() != null ? usuario.getPersona().getApellido() : "N/A";

        return UsuarioDTO.builder()
                .idUsuario(usuario.getId())
                .nombre(nombre)
                .apellido(apellido)
                .email(usuario.getEmail())
                .nombreCompleto(nombre + " " + apellido)
                .build();
    }

    protected DepartamentoDTO mapDepartamento(Departamento departamento) {
        if (departamento == null) {
            return null;
        }

        return DepartamentoDTO.builder()
                .idDepartamento(departamento.getId())
                .nombre(departamento.getNombreDepartamento())
                .codigo("DEPT-" + departamento.getId())
                .build();
    }

    @Named("mapTipoEstructura")
    protected TipoEstructuraDTO mapTipoEstructura(TipoEstructuraEntity tipoEstructura) {
        return tipoEstructuraMapper.toDTO(tipoEstructura);
    }
}