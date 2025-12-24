package com.upeu.gestioninventario.dashboard.service;

import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.categorias.repository.CategoriaRepository;
import com.upeu.gestioninventario.dashboard.dto.*;
import com.upeu.gestioninventario.estructuras.repository.AmbienteRepository;
import com.upeu.gestioninventario.estructuras.repository.EdificioRepository;
import com.upeu.gestioninventario.estructuras.repository.EstacionRepository;
import com.upeu.gestioninventario.estructuras.repository.PisoRepository;
import com.upeu.gestioninventario.inventario.repository.BienRepository;
import com.upeu.gestioninventario.personas.dto.BienResumenDTO;
import com.upeu.gestioninventario.personas.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final BienRepository bienRepository;
    private final CategoriaRepository categoriaRepository;
    private final EstacionRepository estacionRepository;
    private final EdificioRepository edificioRepository;
    private final PisoRepository pisoRepository;
    private final AmbienteRepository ambienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;

    @Override
    public DashboardResumenDTO obtenerResumenCompleto() {
        return new DashboardResumenDTO(
                obtenerConteos(),
                obtenerBienesPorCategoria(),
                obtenerResumenEstructura(),
                obtenerEstacionesPorAmbiente(),
                obtenerUltimosBienes(),
                obtenerAlertas()
        );
    }

    @Override
    public ConteoGeneralDTO obtenerConteos() {
        return new ConteoGeneralDTO(
                bienRepository.count(),
                categoriaRepository.countByVisibleTrue(),
                estacionRepository.count(),
                personaRepository.count(),
                usuarioRepository.findByActivoTrueWithPersona().size(),
                usuarioRepository.findByActivoFalseWithPersona().size()
        );
    }

    @Override
    public List<BienesPorCategoriaDTO> obtenerBienesPorCategoria() {
        return bienRepository.countBienesPorCategoriaAgrupado().stream()
                .map(row -> new BienesPorCategoriaDTO(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2]
                ))
                .toList();
    }

    @Override
    public EstructuraResumenDTO obtenerResumenEstructura() {
        return new EstructuraResumenDTO(
                edificioRepository.countEdificiosActivos(),
                pisoRepository.count(),
                ambienteRepository.count(),
                estacionRepository.count(),
                estacionRepository.findEstacionesConBienesAsignados().size(),
                estacionRepository.findEstacionesSinBienesAsignados().size()
        );
    }

    @Override
    public List<EstacionesPorAmbienteDTO> obtenerEstacionesPorAmbiente() {
        return estacionRepository.countEstacionesPorAmbiente().stream()
                .map(row -> new EstacionesPorAmbienteDTO(
                        (Long) row[0],
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        (Long) row[4]
                ))
                .toList();
    }

    @Override
    public AlertasDTO obtenerAlertas() {
        return new AlertasDTO(
                personaRepository.countPersonasSinUsuario(),
                usuarioRepository.findByActivoFalseWithPersona().size()
        );
    }

    private List<BienResumenDTO> obtenerUltimosBienes() {
        return bienRepository.findTop10ByOrderByFechaCreacionDesc().stream()
                .map(bien -> new BienResumenDTO(
                        bien.getId(),
                        bien.getNombreBien(),
                        bien.getCaf(),
                        bien.getCategoria() != null ? bien.getCategoria().getNombreCategoria() : null,
                        bien.getUbicacionActual() != null ? bien.getUbicacionActual().getNombreUbicacion() : null
                ))
                .toList();
    }
}
