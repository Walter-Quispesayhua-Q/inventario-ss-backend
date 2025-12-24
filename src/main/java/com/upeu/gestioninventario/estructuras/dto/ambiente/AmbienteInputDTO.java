package com.upeu.gestioninventario.estructuras.dto.ambiente;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmbienteInputDTO {
    
    private String nombre;
    private String codigo;
    private Long idPiso;
    private Long idTipoEstructura;
    private Integer capacidadPersonas;
    private Long idResponsable;
    private Long idDepartamentoResponsable;
    private String observaciones;
    private Map<String, Object> propiedadesAdicionales;
}
