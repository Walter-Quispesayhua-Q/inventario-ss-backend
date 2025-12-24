package com.upeu.gestioninventario.inventario.mapper;


import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.inventario.dto.bien.BienDTO;
import com.upeu.gestioninventario.inventario.model.Bien;
import com.upeu.gestioninventario.ubicaciones.dto.UbicacionDTO;
import com.upeu.gestioninventario.ubicaciones.mapper.UbicacionMapper;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {
    UbicacionMapper.class, 
    AtributoDisplayMapper.class,
    BienAtributoValorMapper.class
})
public interface BienMapper {


    @Named("toDtoBase")
    @Mappings({
        // Campos básicos
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "nombreBien", target = "nombreBien"),
        @Mapping(source = "caf", target = "caf"),
        @Mapping(source = "numeroSerie", target = "numeroSerie"),
        @Mapping(source = "observaciones", target = "observaciones"),
        
        // Estados
        @Mapping(source = "estadoFisico", target = "estadoFisico"),
        @Mapping(source = "estadoOperacional", target = "estadoOperacional"),
        
        // Categoría
        @Mapping(source = "categoria.id", target = "categoriaId"),
        @Mapping(source = "categoria.nombreCategoria", target = "categoriaNombre"),
        @Mapping(source = "categoria.icono", target = "categoriaIcono"),
        @Mapping(source = "categoria.color", target = "categoriaColor"),
        
        // Ubicación actual
        @Mapping(source = "ubicacionActual", target = "ubicacionActual"),
        
        // Información de estación (V3.0)
        @Mapping(target = "estaEnEstacion", expression = "java(entity.getAsignacionEstacionActiva() != null)"),
        @Mapping(source = "asignacionEstacionActiva.estacion.idEstacion", target = "estacionActualId"),
        @Mapping(source = "asignacionEstacionActiva.estacion.nombre", target = "estacionActualNombre"),
        @Mapping(source = "asignacionEstacionActiva.estacion.codigo", target = "estacionActualCodigo"),
        
        // Responsable
        @Mapping(source = "responsableActual.id", target = "responsableActualId"),
        @Mapping(source = "responsableActual", target = "responsableActualNombre", qualifiedByName = "personaToFullName"),

        // Departamento
        @Mapping(source = "departamento.id", target = "departamentoId"),
        @Mapping(source = "departamento.nombreDepartamento", target = "departamentoNombre"),
        
        // Auditoría
        @Mapping(source = "usuarioCreacion.persona", target = "usuarioCreacionNombre", qualifiedByName = "personaToFullName"),
        @Mapping(source = "usuarioUltimaModificacion.persona", target = "usuarioUltimaModificacionNombre", qualifiedByName = "personaToFullName"),
        @Mapping(source = "fechaCreacion", target = "fechaCreacion"),
        @Mapping(source = "fechaUltimaModificacion", target = "fechaUltimaModificacion"),
        
        // Atributos - Mapear BienAtributoValor utilizando el mapper correspondiente
        @Mapping(source = "atributos", target = "atributosValores")
    })
    BienDTO toDtoBase(Bien entity);

    @Named("toDto")
    default BienDTO toDto(Bien entity) {
        if (entity == null) {
            return null;
        }

        BienDTO dtoBase = toDtoBase(entity);
        UbicacionDTO ubicacionJerarquica = construirUbicacionJerarquica(entity);
        return new BienDTO(
                dtoBase.id(),
                dtoBase.nombreBien(),
                dtoBase.caf(),
                dtoBase.numeroSerie(),
                dtoBase.observaciones(),
                dtoBase.estadoFisico(),
                dtoBase.estadoOperacional(),
                dtoBase.fechaCreacion(),
                dtoBase.usuarioCreacionNombre(),
                dtoBase.fechaUltimaModificacion(),
                dtoBase.usuarioUltimaModificacionNombre(),
                dtoBase.categoriaId(),
                dtoBase.categoriaNombre(),
                dtoBase.categoriaIcono(),
                dtoBase.categoriaColor(),
                ubicacionJerarquica,
                dtoBase.estaEnEstacion(),
                dtoBase.estacionActualId(),
                dtoBase.estacionActualNombre(),
                dtoBase.estacionActualCodigo(),
                dtoBase.responsableActualId(),
                dtoBase.responsableActualNombre(),
                dtoBase.departamentoId(),
                dtoBase.departamentoNombre(),
                dtoBase.atributosValores()
        );
    }


    @Named("personaToFullName")
    default String personaToFullName(Persona persona) {
        if (persona == null) {
            return null;
        }
        String nombre = persona.getNombre() != null ? persona.getNombre() : "";
        String apellido = persona.getApellido() != null ? persona.getApellido() : "";
        return (nombre + " " + apellido).trim();
    }

    default UbicacionDTO construirUbicacionJerarquica(Bien bien) {
        if (bien == null) {
            return null;
        }
        return construirUbicacionSimpleDesdeActual(bien);
    }

    default UbicacionDTO construirUbicacionSimpleDesdeActual(Bien bien) {
        Ubicacion ubicacion = bien.getUbicacionActual();
        
        if (ubicacion == null) {
            return new UbicacionDTO(null, "No asignada", null, null, null, null);
        }

        // Si la ubicación tiene detalles (edificio, piso, ambiente), construir el nombre completo
        String nombreUbicacion;
        boolean tieneDetalles = (ubicacion.getEdificio() != null && !ubicacion.getEdificio().isBlank()) ||
                               (ubicacion.getPiso() != null && !ubicacion.getPiso().isBlank()) ||
                               (ubicacion.getOficinaAmbiente() != null && !ubicacion.getOficinaAmbiente().isBlank());
        
        // V3.0: Verificar si el bien está en una estación para incluirla en la jerarquía
        String codigoEstacion = null;
        if (bien.getAsignacionEstacionActiva() != null && 
            bien.getAsignacionEstacionActiva().getEstacion() != null) {
            codigoEstacion = bien.getAsignacionEstacionActiva().getEstacion().getCodigo();
        }
        
        if (tieneDetalles) {
            // Ubicación detallada: "Edificio > Piso > Ambiente [> Estación]"
            nombreUbicacion = construirNombreUbicacionCompleto(
                    ubicacion.getEdificio(), 
                    ubicacion.getPiso(), 
                    ubicacion.getOficinaAmbiente(),
                    codigoEstacion
            );
        } else if (codigoEstacion != null) {
            // Ubicación simple + estación: "Lab. Software > PC-01"
            nombreUbicacion = ubicacion.getNombreUbicacion() + " > " + codigoEstacion;
        } else {
            // Ubicación simple registrada: ej "Lab. Software"
            nombreUbicacion = ubicacion.getNombreUbicacion();
        }

        return new UbicacionDTO(
                ubicacion.getId(),
                nombreUbicacion,
                ubicacion.getDescripcion(),
                ubicacion.getEdificio(),
                ubicacion.getPiso(),
                ubicacion.getOficinaAmbiente()
        );
    }


    default String construirNombreUbicacionCompleto(String edificio, String piso, String ambiente, String estacion) {
        StringBuilder sb = new StringBuilder();
        
        if (edificio != null && !edificio.isEmpty()) {
            sb.append(edificio);
        }
        
        if (piso != null && !piso.isEmpty()) {
            if (sb.length() > 0) sb.append(" > ");
            sb.append(piso);
        }
        
        if (ambiente != null && !ambiente.isEmpty()) {
            if (sb.length() > 0) sb.append(" > ");
            sb.append(ambiente);
        }
        
        // V3.0: Incluir estación si existe
        if (estacion != null && !estacion.isEmpty()) {
            if (sb.length() > 0) sb.append(" > ");
            sb.append(estacion);
        }
        
        return sb.length() > 0 ? sb.toString() : "Sin ubicación";
    }

}