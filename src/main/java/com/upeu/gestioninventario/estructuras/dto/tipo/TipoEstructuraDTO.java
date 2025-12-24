package com.upeu.gestioninventario.estructuras.dto.tipo;

import com.upeu.gestioninventario.estructuras.model.NivelEstructura;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoEstructuraDTO {
    
    private Long idTipo;
    private String nombre;
    private String codigo;
    private NivelEstructura nivelAplicable;
    private String descripcion;
    private String icono;
    private String color;
    private Map<String, Object> configuracionCampos;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
}
