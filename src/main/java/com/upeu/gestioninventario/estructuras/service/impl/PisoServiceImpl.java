package com.upeu.gestioninventario.estructuras.service.impl;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.estructuras.dto.FiltroPisoDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoDTO;
import com.upeu.gestioninventario.estructuras.dto.piso.PisoInputDTO;
import com.upeu.gestioninventario.estructuras.mapper.PisoMapper;
import com.upeu.gestioninventario.estructuras.model.Edificio;
import com.upeu.gestioninventario.estructuras.model.Piso;
import com.upeu.gestioninventario.estructuras.model.TipoEstructuraEntity;
import com.upeu.gestioninventario.estructuras.repository.AmbienteRepository;
import com.upeu.gestioninventario.estructuras.repository.EdificioRepository;
import com.upeu.gestioninventario.estructuras.repository.PisoRepository;
import com.upeu.gestioninventario.estructuras.repository.TipoEstructuraRepository;
import com.upeu.gestioninventario.estructuras.service.IPisoService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PisoServiceImpl implements IPisoService {

    private final PisoRepository pisoRepository;
    private final EdificioRepository edificioRepository;
    private final TipoEstructuraRepository tipoEstructuraRepository;
    private final UsuarioRepository usuarioRepository;
    private final AmbienteRepository ambienteRepository;
    private final PisoMapper pisoMapper;

    @Override
    public OperacionResultadoDTO<PisoDTO> crearPiso(PisoInputDTO inputDTO) {
        try {
            log.info("Creando nuevo piso: {} en edificio ID: {}", inputDTO.getNombre(), inputDTO.getIdEdificio());

            Edificio edificio = edificioRepository.findById(inputDTO.getIdEdificio())
                    .orElseThrow(() -> new IllegalArgumentException("Edificio no encontrado con ID: " + inputDTO.getIdEdificio()));

            if (pisoRepository.existsByEdificioIdEdificioAndCodigo(inputDTO.getIdEdificio(), inputDTO.getCodigo())) {
                log.warn("Intento de crear piso con código duplicado: {} en edificio: {}", inputDTO.getCodigo(), edificio.getNombre());
                return OperacionResultadoDTO.error("Ya existe un piso con el código '" + inputDTO.getCodigo() + "' en el edificio '" + edificio.getNombre() + "'");
            }

            if (pisoRepository.existsByEdificioIdEdificioAndNumeroPiso(inputDTO.getIdEdificio(), inputDTO.getNumeroPiso())) {
                log.warn("Intento de crear piso con número duplicado: {} en edificio: {}", inputDTO.getNumeroPiso(), edificio.getNombre());
                return OperacionResultadoDTO.error("Ya existe el piso número " + inputDTO.getNumeroPiso() + " en el edificio '" + edificio.getNombre() + "'");
            }

            TipoEstructuraEntity tipoEstructura = tipoEstructuraRepository.findById(inputDTO.getIdTipoEstructura())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de estructura no encontrado"));

            if (!tipoEstructura.getActivo()) {
                return OperacionResultadoDTO.error("El tipo de estructura no está activo");
            }

            Usuario responsable = null;
            if (inputDTO.getIdResponsableMantenimiento() != null) {
                responsable = usuarioRepository.findById(inputDTO.getIdResponsableMantenimiento())
                        .orElseThrow(() -> new IllegalArgumentException("Usuario responsable no encontrado"));
            }

            Piso piso = pisoMapper.toEntity(inputDTO);
            piso.setEdificio(edificio);
            piso.setTipoEstructura(tipoEstructura);
            piso.setResponsableMantenimiento(responsable);

            Piso pisoGuardado = pisoRepository.save(piso);

            log.info("Piso creado exitosamente: {} (ID: {}) en edificio: {}", pisoGuardado.getNombre(), pisoGuardado.getIdPiso(), edificio.getNombre());
            return OperacionResultadoDTO.exito("Piso creado exitosamente", pisoMapper.toDTO(pisoGuardado));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear piso: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al crear piso", e);
            return OperacionResultadoDTO.error("Error al crear el piso");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PisoDTO obtenerPisoPorId(Long idPiso) {
        log.info("Obteniendo piso con ID: {}", idPiso);
        Piso piso = buscarPisoPorId(idPiso);
        return pisoMapper.toDTO(piso);
    }


    @Override
    @Transactional(readOnly = true)
    public PisoDTO obtenerPisoPorEdificioYNumero(Long idEdificio, Integer numeroPiso) {
        log.info("Obteniendo piso numero: {} del edificio: {}", numeroPiso, idEdificio);

        Piso piso = pisoRepository.findByEdificioIdEdificioAndNumeroPiso(idEdificio, numeroPiso)
                .orElseThrow(() -> new RuntimeException("Piso numero " + numeroPiso + " no encontrado en el edificio"));

        return pisoMapper.toDTO(piso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PisoDTO> listarPisosPorEdificio(Long idEdificio) {
        log.info("Listando pisos del edificio ID: {}", idEdificio);

        if (!edificioRepository.existsById(idEdificio)) {
            throw new RuntimeException("Edificio no encontrado con ID: " + idEdificio);
        }

        List<Piso> pisos = pisoRepository.findByEdificioWithDetails(idEdificio);
        return pisoMapper.toDTOList(pisos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PisoDTO> listarTodosPisos() {
        log.info("Listando todos los pisos activos");
        List<Piso> pisos = pisoRepository.findAll();
        return pisoMapper.toDTOList(pisos);
    }



    @Override
    @Transactional(readOnly = true)
    public List<PisoDTO> buscarPisosConFiltros(FiltroPisoDTO filtro) {
        log.info("Buscando pisos con filtros: {}", filtro);

        List<Piso> pisos = pisoRepository.buscarConFiltros(
                filtro.getNombre(),
                filtro.getCodigo(),
                filtro.getIdEdificio(),
                filtro.getNumeroPiso(),
                filtro.getIdTipoEstructura()
        );

        return pisoMapper.toDTOList(pisos);
    }

    @Override
    public OperacionResultadoDTO<PisoDTO> actualizarPiso(Long idPiso, PisoInputDTO inputDTO) {
        try {
            log.info("Actualizando piso ID: {}", idPiso);

            Piso piso = pisoRepository.findById(idPiso)
                    .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado con ID: " + idPiso));

            Long idEdificioActual = piso.getEdificio().getIdEdificio();
            Long idEdificioNuevo = inputDTO.getIdEdificio() != null ? inputDTO.getIdEdificio() : idEdificioActual;

            if (inputDTO.getCodigo() != null && !inputDTO.getCodigo().equals(piso.getCodigo())) {
                if (pisoRepository.existsByEdificioIdEdificioAndCodigo(idEdificioNuevo, inputDTO.getCodigo())) {
                    log.warn("Intento de actualizar con código duplicado: {}", inputDTO.getCodigo());
                    return OperacionResultadoDTO.error("Ya existe un piso con el código: " + inputDTO.getCodigo());
                }
            }

            if (inputDTO.getNumeroPiso() != null && !inputDTO.getNumeroPiso().equals(piso.getNumeroPiso())) {
                if (pisoRepository.existsByEdificioIdEdificioAndNumeroPiso(idEdificioNuevo, inputDTO.getNumeroPiso())) {
                    log.warn("Intento de actualizar con número duplicado: {}", inputDTO.getNumeroPiso());
                    return OperacionResultadoDTO.error("Ya existe el piso número: " + inputDTO.getNumeroPiso());
                }
            }

            if (inputDTO.getIdEdificio() != null && !inputDTO.getIdEdificio().equals(idEdificioActual)) {
                Edificio edificioNuevo = edificioRepository.findById(inputDTO.getIdEdificio())
                        .orElseThrow(() -> new IllegalArgumentException("Edificio no encontrado con ID: " + inputDTO.getIdEdificio()));
                piso.setEdificio(edificioNuevo);
            }

            if (inputDTO.getIdTipoEstructura() != null) {
                TipoEstructuraEntity tipoEstructura = tipoEstructuraRepository.findById(inputDTO.getIdTipoEstructura())
                        .orElseThrow(() -> new IllegalArgumentException("Tipo de estructura no encontrado"));
                if (!tipoEstructura.getActivo()) {
                    return OperacionResultadoDTO.error("El tipo de estructura no está activo");
                }
                piso.setTipoEstructura(tipoEstructura);
            }

            if (inputDTO.getIdResponsableMantenimiento() != null) {
                Usuario responsable = usuarioRepository.findById(inputDTO.getIdResponsableMantenimiento())
                        .orElseThrow(() -> new IllegalArgumentException("Usuario responsable no encontrado"));
                piso.setResponsableMantenimiento(responsable);
            }

            pisoMapper.updateEntityFromInput(inputDTO, piso);
            Piso pisoActualizado = pisoRepository.save(piso);

            log.info("Piso actualizado exitosamente ID: {}", idPiso);
            return OperacionResultadoDTO.exito("Piso actualizado exitosamente", pisoMapper.toDTO(pisoActualizado));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar piso ID {}: {}", idPiso, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al actualizar piso ID {}", idPiso, e);
            return OperacionResultadoDTO.error("Error al actualizar el piso");
        }
    }

    @Override
    public OperacionResultadoDTO<Boolean> eliminarPiso(Long idPiso) {
        try {
            log.info("Eliminando piso ID: {}", idPiso);

            Piso piso = pisoRepository.findById(idPiso)
                    .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado con ID: " + idPiso));

            long cantidadAmbientes = ambienteRepository.countByPisoIdPiso(idPiso);
            if (cantidadAmbientes > 0) {
                log.warn("Intento de eliminar piso con ambientes asociados ID: {}", idPiso);
                return OperacionResultadoDTO.error(
                        String.format("No se puede eliminar el piso porque tiene %d ambiente(s) asociado(s)", cantidadAmbientes)
                );
            }

            pisoRepository.delete(piso);
            log.info("Piso eliminado exitosamente: {}", piso.getNombre());
            return OperacionResultadoDTO.exito("Piso eliminado exitosamente", true);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al eliminar piso ID {}: {}", idPiso, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al eliminar piso ID {}", idPiso, e);
            return OperacionResultadoDTO.error("Error al eliminar el piso");
        }
    }

    // MÉTODOS AUXILIARES PRIVADOS

    private Piso buscarPisoPorId(Long idPiso) {
        return pisoRepository.findById(idPiso)
                .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado con ID: " + idPiso));
    }
}