package com.upeu.gestioninventario.estructuras.dto.estacion;

import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstacionConComponentesDTO {

    private Long idEstacion;
    private String nombre;
    private String codigo;
    private Integer capacidadBienes;

    private Long idAmbiente;
    private String nombreAmbiente;
    private String nombrePiso;
    private String nombreEdificio;
    
    private String responsableDetectado;

    private List<ComponenteEstacionDTO> componentes;

    private Integer totalComponentes;
    private Integer totalBienes;
    private Integer capacidadDisponible;

    private TipoEstructuraDTO tipoEstructura;
    private String observaciones;
    private LocalDateTime fechaCreacion;

}
