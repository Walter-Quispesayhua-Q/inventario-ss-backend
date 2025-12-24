package com.upeu.gestioninventario.dashboard.dto;

import com.upeu.gestioninventario.personas.dto.BienResumenDTO;

import java.util.List;

public record DashboardResumenDTO(
        ConteoGeneralDTO conteos,
        List<BienesPorCategoriaDTO> bienesPorCategoria,
        EstructuraResumenDTO estructura,
        List<EstacionesPorAmbienteDTO> estacionesPorAmbiente,
        List<BienResumenDTO> ultimosBienes,
        AlertasDTO alertas
) {}
