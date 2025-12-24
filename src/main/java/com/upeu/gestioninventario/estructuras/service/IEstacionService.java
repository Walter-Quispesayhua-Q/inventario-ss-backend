package com.upeu.gestioninventario.estructuras.service;

import com.upeu.gestioninventario.estructuras.dto.BienesParaEstacionResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.BienEstacionPaginadoDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionInputDTO;
import com.upeu.gestioninventario.estructuras.model.OrdenEstacion;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.util.List;

public interface IEstacionService {

    OperacionResultadoDTO<EstacionDTO> crearEstacion(EstacionInputDTO inputDTO);

    EstacionDTO obtenerEstacionPorId(Long idEstacion);

    List<EstacionDTO> listarEstacionesPorAmbiente(Long idAmbiente);

    List<EstacionDTO> listarTodasEstaciones();

    List<EstacionDTO> obtenerEstacionesConCapacidadDisponible();

    List<EstacionDTO> buscarEstacionesConFiltros(FiltroEstacionDTO filtro, OrdenEstacion orden);

    BienEstacionPaginadoDTO obtenerBienesDeEstacionPaginados(Long idEstacion, int pagina, int tamanoPagina);

    OperacionResultadoDTO<EstacionDTO> actualizarEstacion(Long idEstacion, EstacionInputDTO inputDTO);

    OperacionResultadoDTO<Boolean> eliminarEstacion(Long idEstacion);

    BienesParaEstacionResponseDTO obtenerBienesParaFormulario(Long idEstacion);
}