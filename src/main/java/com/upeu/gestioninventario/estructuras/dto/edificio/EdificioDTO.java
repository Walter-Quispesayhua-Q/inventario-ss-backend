package com.upeu.gestioninventario.estructuras.dto.edificio;

import com.upeu.gestioninventario.estructuras.dto.piso.PisoDTO;
import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdificioDTO {
    
    private Long idEdificio;
    private String nombre;
    private String codigo;
    private TipoEstructuraDTO tipoEstructura;
    
    // Ubicación geográfica
    private String direccion;
    private BigDecimal coordenadasLatitud;
    private BigDecimal coordenadasLongitud;
    
    // Características físicas
    private Integer numeroPisos;
    private BigDecimal areaTotalM2;
    private Integer anoConstruccion;
    
    // Responsabilidad
    private Long idResponsableMantenimiento;
    private String nombreResponsableMantenimiento;
    
    // Observaciones y metadata
    private String observaciones;
    private Map<String, Object> propiedadesAdicionales;
    
    // Auditoría
    private Long idUsuarioCreacion;
    private Long idUsuarioUltimaModificacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private LocalDateTime fechaEliminacion;
    
    // Jerarquía
    private List<PisoDTO> pisos;
    private Long cantidadPisos;
}
