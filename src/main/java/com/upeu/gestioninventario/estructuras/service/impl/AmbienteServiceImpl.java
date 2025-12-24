package com.upeu.gestioninventario.estructuras.service.impl;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteDTO;
import com.upeu.gestioninventario.estructuras.dto.ambiente.AmbienteInputDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroAmbienteDTO;
import com.upeu.gestioninventario.estructuras.mapper.AmbienteMapper;
import com.upeu.gestioninventario.estructuras.model.Ambiente;
import com.upeu.gestioninventario.estructuras.model.Piso;
import com.upeu.gestioninventario.estructuras.model.TipoEstructuraEntity;
import com.upeu.gestioninventario.estructuras.repository.AmbienteRepository;
import com.upeu.gestioninventario.estructuras.repository.EstacionRepository;
import com.upeu.gestioninventario.estructuras.repository.PisoRepository;
import com.upeu.gestioninventario.estructuras.repository.TipoEstructuraRepository;
import com.upeu.gestioninventario.estructuras.service.IAmbienteService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import com.upeu.gestioninventario.ubicaciones.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AmbienteServiceImpl implements IAmbienteService {

    private final AmbienteRepository ambienteRepository;
    private final PisoRepository pisoRepository;
    private final TipoEstructuraRepository tipoEstructuraRepository;
    private final UsuarioRepository usuarioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final EstacionRepository estacionRepository;
    private final AmbienteMapper ambienteMapper;

    @Override
    public OperacionResultadoDTO<AmbienteDTO> crearAmbiente(AmbienteInputDTO inputDTO) {
        try {
            log.info("Creando nuevo ambiente: {} en piso ID: {}", inputDTO.getNombre(), inputDTO.getIdPiso());

            Piso piso = pisoRepository.findById(inputDTO.getIdPiso())
                    .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado con ID: " + inputDTO.getIdPiso()));

            if (ambienteRepository.existsByPisoIdPisoAndCodigo(inputDTO.getIdPiso(), inputDTO.getCodigo())) {
                log.warn("Intento de crear ambiente con código duplicado: {} en piso: {}", inputDTO.getCodigo(), piso.getNombre());
                return OperacionResultadoDTO.error("Ya existe un ambiente con el código '" + inputDTO.getCodigo() + "' en el piso '" + piso.getNombre() + "'");
            }

            TipoEstructuraEntity tipoEstructura = tipoEstructuraRepository.findById(inputDTO.getIdTipoEstructura())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de estructura no encontrado"));

            if (!tipoEstructura.getActivo()) {
                return OperacionResultadoDTO.error("El tipo de estructura no está activo");
            }

            Usuario responsable = null;
            if (inputDTO.getIdResponsable() != null) {
                responsable = usuarioRepository.findById(inputDTO.getIdResponsable())
                        .orElseThrow(() -> new IllegalArgumentException("Usuario responsable no encontrado"));
            }

            Departamento departamento = null;
            if (inputDTO.getIdDepartamentoResponsable() != null) {
                departamento = departamentoRepository.findById(inputDTO.getIdDepartamentoResponsable())
                        .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
            }

            Ambiente ambiente = ambienteMapper.toEntity(inputDTO);
            ambiente.setPiso(piso);
            ambiente.setTipoEstructura(tipoEstructura);
            ambiente.setResponsable(responsable);
            ambiente.setDepartamentoResponsable(departamento);

            Ambiente ambienteGuardado = ambienteRepository.save(ambiente);

            log.info("Ambiente creado exitosamente: {} (ID: {}) en piso: {}", ambienteGuardado.getNombre(), ambienteGuardado.getIdAmbiente(), piso.getNombre());
            return OperacionResultadoDTO.exito("Ambiente creado exitosamente", ambienteMapper.toDTO(ambienteGuardado));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear ambiente: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al crear ambiente", e);
            return OperacionResultadoDTO.error("Error al crear el ambiente");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AmbienteDTO obtenerAmbientePorId(Long idAmbiente) {
        log.info("Obteniendo ambiente con ID: {}", idAmbiente);
        Ambiente ambiente = buscarAmbientePorId(idAmbiente);
        return ambienteMapper.toDTO(ambiente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbienteDTO> listarAmbientesPorPiso(Long idPiso) {
        log.info("Listando ambientes del piso ID: {}", idPiso);

        if (!pisoRepository.existsById(idPiso)) {
            throw new RuntimeException("Piso no encontrado con ID: " + idPiso);
        }

        List<Ambiente> ambientes = ambienteRepository.findByPisoWithDetails(idPiso);
        List<AmbienteDTO> dtos = ambienteMapper.toDTOList(ambientes);
        
        dtos.forEach(dto -> dto.setCantidadEstaciones(
                estacionRepository.countByAmbienteIdAmbiente(dto.getIdAmbiente())
        ));
        
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbienteDTO> listarTodosAmbientes() {
        log.info("Listando todos los ambientes activos");
        List<Ambiente> ambientes = ambienteRepository.findAll();
        return ambienteMapper.toDTOList(ambientes);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarAmbientesPorPiso(Long idPiso) {
        return ambienteRepository.countByPisoIdPiso(idPiso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbienteDTO> buscarAmbientesConFiltros(FiltroAmbienteDTO filtro) {
        log.info("Buscando ambientes con filtros: {}", filtro);

        List<Ambiente> ambientes = ambienteRepository.buscarConFiltros(
                filtro.getNombre(),
                filtro.getCodigo(),
                filtro.getIdPiso(),
                filtro.getIdEdificio(),
                filtro.getIdTipoEstructura(),
                filtro.getIdDepartamentoResponsable()
        );

        return ambienteMapper.toDTOList(ambientes);
    }

    @Override
    public OperacionResultadoDTO<AmbienteDTO> actualizarAmbiente(Long idAmbiente, AmbienteInputDTO inputDTO) {
        try {
            log.info("Actualizando ambiente ID: {}", idAmbiente);

            Ambiente ambiente = ambienteRepository.findById(idAmbiente)
                    .orElseThrow(() -> new IllegalArgumentException("Ambiente no encontrado con ID: " + idAmbiente));

            Long idPisoActual = ambiente.getPiso().getIdPiso();
            Long idPisoNuevo = inputDTO.getIdPiso() != null ? inputDTO.getIdPiso() : idPisoActual;

            if (inputDTO.getCodigo() != null && !inputDTO.getCodigo().equals(ambiente.getCodigo())) {
                if (ambienteRepository.existsByPisoIdPisoAndCodigo(idPisoNuevo, inputDTO.getCodigo())) {
                    log.warn("Intento de actualizar con código duplicado: {}", inputDTO.getCodigo());
                    return OperacionResultadoDTO.error("Ya existe un ambiente con el código: " + inputDTO.getCodigo());
                }
            }

            if (inputDTO.getIdPiso() != null && !inputDTO.getIdPiso().equals(idPisoActual)) {
                Piso pisoNuevo = pisoRepository.findById(inputDTO.getIdPiso())
                        .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado con ID: " + inputDTO.getIdPiso()));
                ambiente.setPiso(pisoNuevo);
            }

            if (inputDTO.getIdTipoEstructura() != null) {
                TipoEstructuraEntity tipoEstructura = tipoEstructuraRepository.findById(inputDTO.getIdTipoEstructura())
                        .orElseThrow(() -> new IllegalArgumentException("Tipo de estructura no encontrado"));
                if (!tipoEstructura.getActivo()) {
                    return OperacionResultadoDTO.error("El tipo de estructura no está activo");
                }
                ambiente.setTipoEstructura(tipoEstructura);
            }

            if (inputDTO.getIdResponsable() != null) {
                Usuario responsable = usuarioRepository.findById(inputDTO.getIdResponsable())
                        .orElseThrow(() -> new IllegalArgumentException("Usuario responsable no encontrado"));
                ambiente.setResponsable(responsable);
            }

            if (inputDTO.getIdDepartamentoResponsable() != null) {
                Departamento departamento = departamentoRepository.findById(inputDTO.getIdDepartamentoResponsable())
                        .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
                ambiente.setDepartamentoResponsable(departamento);
            }

            ambienteMapper.updateEntityFromInput(inputDTO, ambiente);
            Ambiente ambienteActualizado = ambienteRepository.save(ambiente);

            log.info("Ambiente actualizado exitosamente ID: {}", idAmbiente);
            return OperacionResultadoDTO.exito("Ambiente actualizado exitosamente", ambienteMapper.toDTO(ambienteActualizado));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar ambiente ID {}: {}", idAmbiente, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al actualizar ambiente ID {}", idAmbiente, e);
            return OperacionResultadoDTO.error("Error al actualizar el ambiente");
        }
    }

    @Override
    public OperacionResultadoDTO<Boolean> eliminarAmbiente(Long idAmbiente) {
        try {
            log.info("Eliminando ambiente ID: {}", idAmbiente);

            Ambiente ambiente = ambienteRepository.findById(idAmbiente)
                    .orElseThrow(() -> new IllegalArgumentException("Ambiente no encontrado con ID: " + idAmbiente));

            long cantidadEstaciones = estacionRepository.countByAmbienteIdAmbiente(idAmbiente);
            if (cantidadEstaciones > 0) {
                log.warn("Intento de eliminar ambiente con estaciones asociadas ID: {}", idAmbiente);
                return OperacionResultadoDTO.error(
                        String.format("No se puede eliminar el ambiente porque tiene %d estación(es) asociada(s)", cantidadEstaciones)
                );
            }

            ambienteRepository.delete(ambiente);
            log.info("Ambiente eliminado exitosamente: {}", ambiente.getNombre());
            return OperacionResultadoDTO.exito("Ambiente eliminado exitosamente", true);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al eliminar ambiente ID {}: {}", idAmbiente, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al eliminar ambiente ID {}", idAmbiente, e);
            return OperacionResultadoDTO.error("Error al eliminar el ambiente");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeAmbiente(Long idAmbiente) {
        return ambienteRepository.existsById(idAmbiente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbienteDTO> obtenerAmbientesPorDepartamento(Long idDepartamento) {
        log.info("Obteniendo ambientes por departamento: {}", idDepartamento);
        List<Ambiente> ambientes = ambienteRepository.findByDepartamentoResponsableId(idDepartamento);
        return ambienteMapper.toDTOList(ambientes);
    }

    // MÉTODOS AUXILIARES PRIVADOS

    private Ambiente buscarAmbientePorId(Long idAmbiente) {
        return ambienteRepository.findById(idAmbiente)
                .orElseThrow(() -> new IllegalArgumentException("Ambiente no encontrado con ID: " + idAmbiente));
    }
}