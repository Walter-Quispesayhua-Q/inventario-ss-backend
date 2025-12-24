package com.upeu.gestioninventario.ubicaciones.controller;

import com.upeu.gestioninventario.ubicaciones.dto.DepartamentoSimpleDTO;
import com.upeu.gestioninventario.ubicaciones.service.DepartamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @QueryMapping
    public List<DepartamentoSimpleDTO> departamentosDisponibles() {
        return departamentoService.findAllDisponibles();
    }
}