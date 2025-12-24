package com.upeu.gestioninventario.estructuras.dto.estacion;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteEstacionInputDTO {

    private Long idEstacion;

    @NotBlank(message = "El nombre del componente es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de componente es obligatorio")
    private String tipo;

    private String categoriaBase;

    private List<Long> idsBienes;

    private String descripcion;

    private Integer orden;
}
