package com.upeu.gestioninventario.estructuras.service;

import com.upeu.gestioninventario.estructuras.dto.estacion.ComponenteEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.ComponenteEstacionInputDTO;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.util.List;

public interface IComponenteService {

    OperacionResultadoDTO<ComponenteEstacionDTO> crearComponente(ComponenteEstacionInputDTO inputDTO);

    ComponenteEstacionDTO obtenerComponentePorId(Long idComponente);

    List<ComponenteEstacionDTO> listarComponentesPorEstacion(Long idEstacion);

    ComponenteEstacionDTO agregarBienAComponente(Long idComponente, Long idBien, Long idUsuario);

}