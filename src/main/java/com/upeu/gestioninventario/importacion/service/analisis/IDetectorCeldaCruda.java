package com.upeu.gestioninventario.importacion.service.analisis;

import java.util.List;

public interface IDetectorCeldaCruda {

    boolean esFormatoCeldaCruda(List<String> headers, List<List<String>> filasMuestra);

    int obtenerColumnaConDatos(List<String> headers, List<List<String>> filasMuestra);

    double calcularConfianzaCeldaCruda(List<String> headers, List<List<String>> filasMuestra);
}
