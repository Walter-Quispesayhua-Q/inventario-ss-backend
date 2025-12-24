package com.upeu.gestioninventario.importacion.service.analisis;

import com.upeu.gestioninventario.importacion.dto.formato.clavevalor.FormatoClaveValorDTO;
import com.upeu.gestioninventario.importacion.dto.formato.matriz.FormatoMatrizDTO;
import com.upeu.gestioninventario.importacion.dto.formato.tabular.FormatoTabularDTO;


public interface IFormatoConfigLoader {

    FormatoTabularDTO obtenerFormatoTabular();

    FormatoClaveValorDTO obtenerFormatoClaveValor();

    FormatoMatrizDTO obtenerFormatoMatriz();
}
