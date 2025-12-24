package com.upeu.gestioninventario.estructuras.dto.asignacion;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoDesasignacionMasivaDTO {
    private int totalBienes;
    private List<Long> bienesDesasignados;
    private List<Long> bienesNoAsignados;
    private List<String> errores;
    private boolean exito;
    private String mensaje;
}