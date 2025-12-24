package com.upeu.gestioninventario.estructuras.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReglasAgrupacionDTO {

    private String prioridadAgrupacion;
    private Boolean permitirBienEnMultiplesComponentes;
    private Boolean detectarPorNumeroFila;
    private Boolean numerosQueIndicanNuevaFicha;
}