package com.upeu.gestioninventario.estructuras.dto.estacion;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BienEstacionInputDTO {
    
    private Long idBien;
    private Long idEstacion;
    private String posicionRelativa;
    private String observaciones;
}
