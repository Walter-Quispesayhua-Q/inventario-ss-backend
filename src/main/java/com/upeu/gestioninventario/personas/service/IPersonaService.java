package com.upeu.gestioninventario.personas.service;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.personas.dto.PersonaConBienesDTO;
import com.upeu.gestioninventario.personas.dto.PersonaCreacionDTO;
import com.upeu.gestioninventario.personas.dto.PersonaActualizacionDTO;
import com.upeu.gestioninventario.personas.dto.PersonaDTO;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.util.List;

public interface IPersonaService {

    List<PersonaDTO> obtenerTodas();

    PersonaDTO obtenerPorId(Long id);

    PersonaDTO obtenerPorCodigo(String codigo);

    PersonaConBienesDTO obtenerConBienes(Long id);

    List<PersonaDTO> obtenerSinUsuario();

    OperacionResultadoDTO<PersonaDTO> crear(PersonaCreacionDTO dto);

    OperacionResultadoDTO<PersonaDTO> actualizar(Long id, PersonaActualizacionDTO dto);

    OperacionResultadoDTO<Void> eliminar(Long id);

    Persona buscarOCrearResponsable(String nombreResponsableExcel, Usuario usuarioLogueado);

    String normalizarNombre(String nombre);
}
