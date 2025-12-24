package com.upeu.gestioninventario.dashboard.controller;

import com.upeu.gestioninventario.dashboard.dto.*;
import com.upeu.gestioninventario.dashboard.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final IDashboardService dashboardService;

    @QueryMapping
    public DashboardResumenDTO dashboard() {
        log.debug("GraphQL Query: dashboard");
        return dashboardService.obtenerResumenCompleto();
    }

    @QueryMapping
    public ConteoGeneralDTO dashboardConteos() {
        log.debug("GraphQL Query: dashboardConteos");
        return dashboardService.obtenerConteos();
    }

    @QueryMapping
    public List<BienesPorCategoriaDTO> dashboardBienesPorCategoria() {
        log.debug("GraphQL Query: dashboardBienesPorCategoria");
        return dashboardService.obtenerBienesPorCategoria();
    }

    @QueryMapping
    public EstructuraResumenDTO dashboardEstructura() {
        log.debug("GraphQL Query: dashboardEstructura");
        return dashboardService.obtenerResumenEstructura();
    }

    @QueryMapping
    public List<EstacionesPorAmbienteDTO> dashboardEstacionesPorAmbiente() {
        log.debug("GraphQL Query: dashboardEstacionesPorAmbiente");
        return dashboardService.obtenerEstacionesPorAmbiente();
    }

    @QueryMapping
    public AlertasDTO dashboardAlertas() {
        log.debug("GraphQL Query: dashboardAlertas");
        return dashboardService.obtenerAlertas();
    }
}
