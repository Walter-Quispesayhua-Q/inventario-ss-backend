package com.upeu.gestioninventario.estructuras.dto.asignacion;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoDesasignacionDTO {
    private boolean exito;
    private String mensaje;
    private String nombreEstacionAnterior;
}