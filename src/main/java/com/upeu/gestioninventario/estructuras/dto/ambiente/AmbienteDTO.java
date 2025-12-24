package com.upeu.gestioninventario.estructuras.dto.ambiente;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmbienteDTO {
    
    private Long idAmbiente;
    private String nombre;
    private String codigo;
    private Integer capacidadPersonas;
    private BigDecimal areaM2;
    
    // Relación con Piso
    private Long idPiso;
    private String nombrePiso;
    private String codigoPiso;
    private Integer numeroPiso;
    private PisoDTO piso;
    
    // Relación con Edificio
    private Long idEdificio;
    private String nombreEdificio;
    
    // Tipo de estructura
    private TipoEstructuraDTO tipoEstructura;
    
    // Responsabilidad
    private Long idResponsable;
    private String nombreResponsable;
    private Long idDepartamentoResponsable;
    private String nombreDepartamentoResponsable;
    
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
    private List<EstacionDTO> estaciones;
    private Long cantidadEstaciones;
}
