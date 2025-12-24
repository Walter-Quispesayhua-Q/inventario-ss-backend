package com.upeu.gestioninventario.importacion.service.analisis.lectores;

import com.upeu.gestioninventario.importacion.dto.formato.respuesta.HojaAnalizadaDTO;
import org.apache.poi.ss.usermodel.Sheet;

public interface ILectorFormato<T> {

    HojaAnalizadaDTO extraerDatos(Sheet hoja, T configuracion, int indiceHoja);

    boolean soportaFormato(T configuracion);
}
