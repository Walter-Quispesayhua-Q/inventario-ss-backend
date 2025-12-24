package com.upeu.gestioninventario.estructuras.service;

import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteInputDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroAmbienteDTO;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.util.List;

public interface IAmbienteService {

    OperacionResultadoDTO<AmbienteDTO> crearAmbiente(AmbienteInputDTO inputDTO);

    AmbienteDTO obtenerAmbientePorId(Long idAmbiente);

    List<AmbienteDTO> listarAmbientesPorPiso(Long idPiso);

    List<AmbienteDTO> listarTodosAmbientes();

    long contarAmbientesPorPiso(Long idPiso);

    List<AmbienteDTO> buscarAmbientesConFiltros(FiltroAmbienteDTO filtro);

    OperacionResultadoDTO<AmbienteDTO> actualizarAmbiente(Long idAmbiente, AmbienteInputDTO inputDTO);

    OperacionResultadoDTO<Boolean> eliminarAmbiente(Long idAmbiente);

    boolean existeAmbiente(Long idAmbiente);

    List<AmbienteDTO> obtenerAmbientesPorDepartamento(Long idDepartamento);

}