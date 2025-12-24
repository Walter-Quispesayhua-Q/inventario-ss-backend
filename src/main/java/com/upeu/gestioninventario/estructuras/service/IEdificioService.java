package com.upeu.gestioninventario.estructuras.service;

import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioInputDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroEdificioDTO;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.util.List;

public interface IEdificioService {

    OperacionResultadoDTO<EdificioDTO> crearEdificio(EdificioInputDTO inputDTO);

    EdificioDTO obtenerEdificioPorId(Long idEdificio);

    EdificioDTO obtenerEdificioPorCodigo(String codigo);

    List<EdificioDTO> listarEdificios();

    List<EdificioDTO> buscarEdificiosConFiltros(FiltroEdificioDTO filtro);

    OperacionResultadoDTO<EdificioDTO> actualizarEdificio(Long idEdificio, EdificioInputDTO inputDTO);

    OperacionResultadoDTO<Boolean> eliminarEdificio(Long idEdificio);
}