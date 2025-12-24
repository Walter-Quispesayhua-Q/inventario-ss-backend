package com.upeu.gestioninventario.estructuras.dto;

import com.upeu.gestioninventario.estructuras.dto.asignacion.ResultadoAsignacionMasivaDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionDTO;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCreacionEstacionDTO {
    
    private EstacionDTO estacion;
    private ResultadoAsignacionMasivaDTO resultadoAsignacion;
}
