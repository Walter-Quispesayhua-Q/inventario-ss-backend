package com.upeu.gestioninventario.estructuras.dto.estacion;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteEstacionDTO {

    private Long id;
    private String nombre;
    private String tipo;
    private String categoriaBase;
    private Long idEstacionPadre;
    private String nombreEstacion;
    private String codigoEstacion;
    private Integer orden;

    private List<BienEstacionDTO> bienes;

    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private Long idUsuarioCreacion;

    public int totalBienes() {
        return bienes != null ? bienes.size() : 0;
    }


}
