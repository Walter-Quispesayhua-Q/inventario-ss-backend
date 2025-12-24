package com.upeu.gestioninventario.estructuras.service;

import com.upeu.gestioninventario.estructuras.dto.ConfirmacionOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.ResultadoOperacionDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteInputDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioConPisosResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioInputDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConBienesResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionInputDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionResumenDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoConAmbientesResponseDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoInputDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoResumenDTO;


public interface IEstructuraCoordinadorService {

    EdificioConPisosResponseDTO crearEdificioConPisos(EdificioInputDTO input);
    EdificioConPisosResponseDTO obtenerEdificioConPisos(Long idEdificio);
    EdificioConPisosResponseDTO actualizarEdificioConPisos(Long idEdificio, EdificioInputDTO input);
    ResultadoOperacionDTO eliminarEdificio(Long idEdificio, ConfirmacionOperacionDTO confirmacion);
    PisoResumenDTO agregarPisoAEdificio(Long idEdificio, PisoInputDTO input);

    PisoConAmbientesResponseDTO obtenerPisoConAmbientes(Long idPiso);
    PisoConAmbientesResponseDTO actualizarPisoConAmbientes(Long idPiso, PisoInputDTO input);
    ResultadoOperacionDTO eliminarPiso(Long idEdificio, Long idPiso, ConfirmacionOperacionDTO confirmacion);
    AmbienteResumenDTO agregarAmbienteAPiso(Long idPiso, AmbienteInputDTO input);

    AmbienteResumenDTO actualizarAmbiente(Long idAmbiente, AmbienteInputDTO input, ConfirmacionOperacionDTO confirmacion);
    ResultadoOperacionDTO eliminarAmbiente(Long idAmbiente, ConfirmacionOperacionDTO confirmacion);
    EstacionResumenDTO agregarEstacionAAmbiente(Long idAmbiente, EstacionInputDTO input);

    EstacionConBienesResponseDTO obtenerEstacion(Long idEstacion);
    EstacionResumenDTO actualizarEstacion(Long idEstacion, EstacionInputDTO input, ConfirmacionOperacionDTO confirmacion);
    ResultadoOperacionDTO eliminarEstacion(Long idEstacion, ConfirmacionOperacionDTO confirmacion);
}
