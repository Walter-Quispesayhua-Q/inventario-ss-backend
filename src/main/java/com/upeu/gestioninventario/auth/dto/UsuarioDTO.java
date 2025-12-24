package com.upeu.gestioninventario.auth.dto;

import com.upeu.gestioninventario.personas.dto.PersonaDTO;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioDTO(
        Long id,
        String email,
        String codigoUsuario,
        Boolean activo,
        List<String> roles,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaUltimaModificacion,
        LocalDateTime ultimoLogin,
        LocalDateTime fechaEliminacion,
        PersonaDTO persona,
        String departamentoNombre
) {
}
