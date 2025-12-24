package com.upeu.gestioninventario.personas.dto;

import java.time.LocalDateTime;

public record PersonaDTO(
        Long id,
        String nombre,
        String apellido,
        String identificacion,
        String codigo,
        String email,
        String telefono,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaUltimaModificacion,
        Boolean tieneUsuarioVinculado,
        Long usuarioId,
        String usuarioEmail
) {
}