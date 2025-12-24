package com.upeu.gestioninventario.estructuras.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BienYaAsignadoDTO {
    private Long idBien;
    private Long idEstacionActual;
    private String nombreEstacionActual;
}