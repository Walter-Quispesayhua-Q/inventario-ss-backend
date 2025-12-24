package com.upeu.gestioninventario.estructuras.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroPisoDTO {
    
    private String nombre;
    private String codigo;
    private Long idEdificio;
    private Integer numeroPiso;
    private Long idTipoEstructura;
}
