package com.upeu.gestioninventario.importacion.service.analisis;

import com.upeu.gestioninventario.estructuras.model.Ambiente;
import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;

import java.util.Optional;

public interface IBuscadorAmbiente {

    record ResultadoBusqueda(
            Ambiente ambiente,
            double porcentajeCoincidencia,
            String textoOriginal,
            String nombreAmbienteCoincidente
    ) {}

    Optional<ResultadoBusqueda> buscarPorSimilitud(String textoUbicacion, double umbralMinimo);


    double calcularSimilitud(String texto1, String texto2);

    AmbienteDetectadoDTO buscarAmbienteDTO(String textoUbicacion, double umbralMinimo);

}
