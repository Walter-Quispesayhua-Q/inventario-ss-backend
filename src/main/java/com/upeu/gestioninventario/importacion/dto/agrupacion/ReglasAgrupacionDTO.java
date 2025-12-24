package com.upeu.gestioninventario.importacion.dto.agrupacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReglasAgrupacionDTO {
    private String prioridadAgrupacion;
    private boolean permitirBienEnMultiplesComponentes;
    private boolean detectarPorNumeroFila;
    private boolean numerosQueIndicanNuevaFicha;
}
