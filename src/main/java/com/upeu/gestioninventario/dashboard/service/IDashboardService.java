package com.upeu.gestioninventario.dashboard.service;

import com.upeu.gestioninventario.dashboard.dto.*;

import java.util.List;

public interface IDashboardService {

    DashboardResumenDTO obtenerResumenCompleto();

    ConteoGeneralDTO obtenerConteos();

    List<BienesPorCategoriaDTO> obtenerBienesPorCategoria();

    EstructuraResumenDTO obtenerResumenEstructura();

    List<EstacionesPorAmbienteDTO> obtenerEstacionesPorAmbiente();

    AlertasDTO obtenerAlertas();
}
