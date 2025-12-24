package com.upeu.gestioninventario.estructuras.dto.piso;

import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteDTO;
import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PisoDTO {
    
    private Long idPiso;
    private String nombre;
    private String codigo;
    private Integer numeroPiso;
    private BigDecimal areaM2;
    
    // Relación con Edificio
    private Long idEdificio;
    private String nombreEdificio;
    private String codigoEdificio;
    private EdificioDTO edificio; // Solo si se incluye jerarquía completa
    
    // Tipo de estructura
    private TipoEstructuraDTO tipoEstructura;
    
    // Responsabilidad
    private Long idResponsableMantenimiento;
    private String nombreResponsableMantenimiento;
    
    // Observaciones
    private String observaciones;
    private Map<String, Object> propiedadesAdicionales;
    
    // Auditoría
    private Long idUsuarioCreacion;
    private Long idUsuarioUltimaModificacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private LocalDateTime fechaEliminacion;
    
    // Jerarquía
    private List<AmbienteDTO> ambientes;
    private Long cantidadAmbientes;
}
