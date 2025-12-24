package com.upeu.gestioninventario.inventario.service;

import com.upeu.gestioninventario.inventario.dto.bien.*;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.util.List;

public interface IBienService {

    OperacionResultadoDTO<BienDTO> crearBien(BienCreacionInput input);

    OperacionResultadoDTO<BienDTO> actualizarBien(Long id, BienActualizacionInput input);

    BienDTO obtenerBienPorId(Long id);

    BienesPaginados obtenerBienesPaginados(
            Integer page, Integer limit, String buscar,
            Long categoriaId, Long ubicacionId,
            String estadoOperacional, String estadoFisico,
            Long departamentoId, Long responsableId,
            String ordenarPor, String orden
    );

    OperacionResultadoDTO<BienEliminacionResultadoDTO> eliminarBien(Long id);

    List<BienDTO> buscarBienes(String termino);

    List<BienDTO> obtenerBienesPorCategoria(Long categoriaId);
}