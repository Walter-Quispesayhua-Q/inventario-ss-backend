package com.upeu.gestioninventario.estructuras.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroEdificioDTO {
    
    private String nombre;
    private String codigo;
    private Long idTipoEstructura;
    private Long idResponsableMantenimiento;
}
