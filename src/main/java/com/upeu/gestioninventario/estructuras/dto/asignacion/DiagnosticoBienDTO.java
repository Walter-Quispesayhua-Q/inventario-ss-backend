package com.upeu.gestioninventario.estructuras.dto.asignacion;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticoBienDTO {
    private Long idBien;
    private boolean bienExiste;
    private boolean tieneAsignacionActiva;
    private int cantidadAsignacionesActivas;
    private int cantidadAsignacionesTotales;
    private boolean hayInconsistencias;
    private String mensaje;
    private String nombreEstacionActual;
}