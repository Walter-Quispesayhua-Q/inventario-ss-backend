package com.upeu.gestioninventario.estructuras.dto.edificio;

import com.upeu.gestioninventario.estructuras.dto.piso.PisoInputDTO;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdificioInputDTO {
    
    private String nombre;
    private String codigo;
    private Long idTipoEstructura;
    private String direccion;
    private Integer numeroPisos;
    private BigDecimal areaTotalM2;
    private Integer anoConstruccion;
    private Long idResponsableMantenimiento;
    private String observaciones;
    private Map<String, Object> propiedadesAdicionales;
    
    private List<PisoInputDTO> pisos;
}

