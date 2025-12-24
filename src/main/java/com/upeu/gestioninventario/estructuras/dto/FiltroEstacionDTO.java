package com.upeu.gestioninventario.estructuras.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroEstacionDTO {
    
    private String nombre;
    private String codigo;
    private Long idAmbiente;
    private Long idPiso;
    private Long idEdificio;
    private Long idTipoEstructura;
    private Boolean tieneBienesAsignados;
    private String codigoBien;
}
