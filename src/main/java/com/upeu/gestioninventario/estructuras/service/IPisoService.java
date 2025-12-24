package com.upeu.gestioninventario.estructuras.service;

import com.upeu.gestioninventario.estructuras.dto.piso.PisoDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoInputDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroPisoDTO;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.util.List;

public interface IPisoService {

    OperacionResultadoDTO<PisoDTO> crearPiso(PisoInputDTO inputDTO);

    PisoDTO obtenerPisoPorId(Long idPiso);

    PisoDTO obtenerPisoPorEdificioYNumero(Long idEdificio, Integer numeroPiso);

    List<PisoDTO> listarPisosPorEdificio(Long idEdificio);

    List<PisoDTO> listarTodosPisos();

    List<PisoDTO> buscarPisosConFiltros(FiltroPisoDTO filtro);

    OperacionResultadoDTO<PisoDTO> actualizarPiso(Long idPiso, PisoInputDTO inputDTO);

    OperacionResultadoDTO<Boolean> eliminarPiso(Long idPiso);
}