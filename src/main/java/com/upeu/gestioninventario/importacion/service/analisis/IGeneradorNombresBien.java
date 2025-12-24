package com.upeu.gestioninventario.importacion.service.analisis;

import java.util.Map;

public interface IGeneradorNombresBien {

    String generarNombre(Map<String, String> atributosExtraidos, String nombreOriginal);

    String obtenerNombrePlural(String nombreSingular);

    boolean contienePalabra(String texto, String palabra);
}
