package com.upeu.gestioninventario.estructuras.dto.estacion;

import lombok.*;

import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstacionInputDTO {
    
    private String nombre;
    private String codigo;
    private Long idAmbiente;
    private Integer capacidadBienes;
    private Long idTipoEstructura;
    private Long idResponsable;
    private String observaciones;
    private Map<String, Object> propiedadesAdicionales;
}
