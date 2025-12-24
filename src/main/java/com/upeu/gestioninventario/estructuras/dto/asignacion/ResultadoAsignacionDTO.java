package com.upeu.gestioninventario.estructuras.dto.asignacion;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoAsignacionDTO {
    private boolean exito;
    private boolean requiereConfirmacion;
    private String mensaje;
    private Long idAsignacion;
    private Long idEstacionActual;
    private String nombreEstacionActual;
}