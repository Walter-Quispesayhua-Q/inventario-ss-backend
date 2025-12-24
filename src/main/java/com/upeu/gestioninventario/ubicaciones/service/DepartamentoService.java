package com.upeu.gestioninventario.ubicaciones.service;

import com.upeu.gestioninventario.ubicaciones.dto.DepartamentoSimpleDTO;
import com.upeu.gestioninventario.ubicaciones.mapper.DepartamentoMapper;
import com.upeu.gestioninventario.ubicaciones.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final DepartamentoMapper departamentoMapper;

    @Transactional(readOnly = true)
    public List<DepartamentoSimpleDTO> findAllDisponibles() {
        return departamentoRepository.findAll().stream()
                .map(departamentoMapper::toSimpleDto)
                .collect(Collectors.toList());
    }
}