package com.upeu.gestioninventario.estructuras.dto.asignacion;

import com.upeu.gestioninventario.estructuras.dto.BienYaAsignadoDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoAsignacionMasivaDTO {
    private int totalBienes;
    private int bienesLibresAsignados;
    private List<BienYaAsignadoDTO> bienesYaAsignados;
    private List<Long> bienesAsignadosExitosamente;
    private List<String> errores;
    private boolean requiereConfirmacion;
}