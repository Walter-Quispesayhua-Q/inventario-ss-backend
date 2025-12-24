package com.upeu.gestioninventario.importacion.service.analisis;

import com.upeu.gestioninventario.importacion.dto.ColumnaDetectadaDTO;

import java.util.List;

public interface IMapeadorDatos {

    List<ColumnaDetectadaDTO> mapearNombres(List<String> nombres);

    String mapearClave(String clave);

    boolean esCampoConocido(String texto);
}
