package com.upeu.gestioninventario.estructuras.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroAmbienteDTO {
    
    private String nombre;
    private String codigo;
    private Long idPiso;
    private Long idEdificio;
    private Long idTipoEstructura;
    private Long idDepartamentoResponsable;
}
