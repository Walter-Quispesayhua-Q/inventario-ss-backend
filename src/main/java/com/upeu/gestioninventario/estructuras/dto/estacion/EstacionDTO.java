package com.upeu.gestioninventario.estructuras.dto.estacion;

import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstacionDTO {
    
    private Long idEstacion;
    private String nombre;
    private String codigo;
    private BigDecimal posicionX;
    private BigDecimal posicionY;
    private Integer capacidadBienes;
    
    // Relación con Ambiente
    private Long idAmbiente;
    private String nombreAmbiente;
    private String codigoAmbiente;
    private AmbienteDTO ambiente;
    
    // Relación con Piso
    private Long idPiso;
    private String nombrePiso;
    private Integer numeroPiso;
    
    // Relación con Edificio
    private Long idEdificio;
    private String nombreEdificio;
    
    // Tipo de estructura
    private TipoEstructuraDTO tipoEstructura;
    
    // Responsable
    private Long idResponsable;
    private String nombreResponsable;
    
    // Observaciones
    private String observaciones;
    private Map<String, Object> propiedadesAdicionales;
    
    // Auditoría
    private Long idUsuarioCreacion;
    private Long idUsuarioUltimaModificacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private LocalDateTime fechaEliminacion;
    
    // Estadísticas
    private Long cantidadBienesAsignados;
    private Integer capacidadDisponible;
    private Long bienesAsignados;
    private Long bienesDisponibles;
}
