package com.upeu.gestioninventario.estructuras.dto.piso;

import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteInputDTO;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PisoInputDTO {
    
    private Long idPiso;
    private String nombre;
    private String codigo;
    private Long idEdificio;
    private Long idTipoEstructura;
    private Integer numeroPiso;
    private Long idResponsableMantenimiento;
    private String observaciones;
    private Map<String, Object> propiedadesAdicionales;
    
    private List<AmbienteInputDTO> ambientes;
}

