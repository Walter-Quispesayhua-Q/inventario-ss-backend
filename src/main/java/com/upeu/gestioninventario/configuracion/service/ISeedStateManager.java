package com.upeu.gestioninventario.configuracion.service;

public interface ISeedStateManager {

    boolean necesitaRecarga(String nombreArchivo, String hashActual);

    void registrarCarga(String nombreArchivo, String rutaArchivo, String tipoSeed, String hashContenido);

}
