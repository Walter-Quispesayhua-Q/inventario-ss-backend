package com.upeu.gestioninventario.estructuras.dto.asignacion;

import com.upeu.gestioninventario.estructuras.dto.BienYaAsignadoDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoGestionBienesDTO {
    private Long idEstacion;
    private String nombreEstacion;
    private ResultadoAsignacionMasivaDTO resultadoAgrupacion;
    private ResultadoDesasignacionMasivaDTO resultadoDesagrupacion;
    private List<BienYaAsignadoDTO> bienesEnConflicto;
    private boolean requiereConfirmacion;
    private boolean exito;
    private String mensaje;
}