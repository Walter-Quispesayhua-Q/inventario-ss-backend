package com.upeu.gestioninventario.personas.dto;

import java.util.List;

public record PersonaConBienesDTO(
        PersonaDTO persona,
        List<BienResumenDTO> bienesACargo,
        Integer totalBienes
) {
    public static PersonaConBienesDTO of(PersonaDTO persona, List<BienResumenDTO> bienes) {
        return new PersonaConBienesDTO(persona, bienes, bienes.size());
    }
}
