package com.upeu.gestioninventario.estructuras.service.impl;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioDTO;
import com.upeu.gestioninventario.estructuras.dto.edificio.EdificioInputDTO;
import com.upeu.gestioninventario.estructuras.dto.FiltroEdificioDTO;
import com.upeu.gestioninventario.estructuras.mapper.EdificioMapper;
import com.upeu.gestioninventario.estructuras.model.Edificio;
import com.upeu.gestioninventario.estructuras.model.TipoEstructuraEntity;
import com.upeu.gestioninventario.estructuras.repository.EdificioRepository;
import com.upeu.gestioninventario.estructuras.repository.PisoRepository;
import com.upeu.gestioninventario.estructuras.repository.TipoEstructuraRepository;
import com.upeu.gestioninventario.estructuras.service.IEdificioService;
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
public class EdificioServiceImpl implements IEdificioService {

    private final EdificioRepository edificioRepository;
    private final TipoEstructuraRepository tipoEstructuraRepository;
    private final UsuarioRepository usuarioRepository;
    private final PisoRepository pisoRepository;
    private final EdificioMapper edificioMapper;

    @Override
    public OperacionResultadoDTO<EdificioDTO> crearEdificio(EdificioInputDTO inputDTO) {
        try {
            log.info("Creando nuevo edificio: {}", inputDTO.getNombre());

            if (edificioRepository.existsByCodigo(inputDTO.getCodigo())) {
                log.warn("Intento de crear edificio con código duplicado: {}", inputDTO.getCodigo());
                return OperacionResultadoDTO.error("Ya existe un edificio con el código: " + inputDTO.getCodigo());
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

            Edificio edificio = edificioMapper.toEntity(inputDTO);
            edificio.setTipoEstructura(tipoEstructura);
            edificio.setResponsableMantenimiento(responsable);

            Edificio edificioGuardado = edificioRepository.save(edificio);

            log.info("Edificio creado exitosamente: {} (ID: {})", edificioGuardado.getNombre(), edificioGuardado.getIdEdificio());
            return OperacionResultadoDTO.exito("Edificio creado exitosamente", edificioMapper.toDTO(edificioGuardado));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear edificio: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al crear edificio", e);
            return OperacionResultadoDTO.error("Error al crear el edificio");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EdificioDTO obtenerEdificioPorId(Long idEdificio) {
        log.info("Obteniendo edificio con ID: {}", idEdificio);
        Edificio edificio = buscarEdificioPorId(idEdificio);
        return edificioMapper.toDTO(edificio);
    }

    @Override
    @Transactional(readOnly = true)
    public EdificioDTO obtenerEdificioPorCodigo(String codigo) {
        log.info("Obteniendo edificio con codigo: {}", codigo);
        Edificio edificio = edificioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Edificio no encontrado con codigo: " + codigo));
        return edificioMapper.toDTO(edificio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EdificioDTO> listarEdificios() {
        log.info("Listando todos los edificios activos");
        List<Edificio> edificios = edificioRepository.findAllWithTipoEstructura();
        return edificioMapper.toDTOList(edificios);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EdificioDTO> buscarEdificiosConFiltros(FiltroEdificioDTO filtro) {
        log.info("Buscando edificios con filtros: {}", filtro);

        List<Edificio> edificios = edificioRepository.buscarConFiltros(
                filtro.getNombre(),
                filtro.getCodigo(),
                filtro.getIdTipoEstructura(),
                filtro.getIdResponsableMantenimiento()
        );

        return edificioMapper.toDTOList(edificios);
    }



    @Override
    public OperacionResultadoDTO<EdificioDTO> actualizarEdificio(Long idEdificio, EdificioInputDTO inputDTO) {
        try {
            log.info("Actualizando edificio ID: {}", idEdificio);

            Edificio edificio = edificioRepository.findById(idEdificio)
                    .orElseThrow(() -> new IllegalArgumentException("Edificio no encontrado con ID: " + idEdificio));

            if (inputDTO.getCodigo() != null && !inputDTO.getCodigo().equals(edificio.getCodigo())) {
                if (edificioRepository.existsByCodigo(inputDTO.getCodigo())) {
                    log.warn("Intento de actualizar con código duplicado: {}", inputDTO.getCodigo());
                    return OperacionResultadoDTO.error("Ya existe un edificio con el código: " + inputDTO.getCodigo());
                }
            }

            if (inputDTO.getIdTipoEstructura() != null) {
                TipoEstructuraEntity tipoEstructura = tipoEstructuraRepository.findById(inputDTO.getIdTipoEstructura())
                        .orElseThrow(() -> new IllegalArgumentException("Tipo de estructura no encontrado"));
                if (!tipoEstructura.getActivo()) {
                    return OperacionResultadoDTO.error("El tipo de estructura no está activo");
                }
                edificio.setTipoEstructura(tipoEstructura);
            }

            if (inputDTO.getIdResponsableMantenimiento() != null) {
                Usuario responsable = usuarioRepository.findById(inputDTO.getIdResponsableMantenimiento())
                        .orElseThrow(() -> new IllegalArgumentException("Usuario responsable no encontrado"));
                edificio.setResponsableMantenimiento(responsable);
            }

            edificioMapper.updateEntityFromInput(inputDTO, edificio);
            Edificio edificioActualizado = edificioRepository.save(edificio);

            log.info("Edificio actualizado exitosamente ID: {}", idEdificio);
            return OperacionResultadoDTO.exito("Edificio actualizado exitosamente", edificioMapper.toDTO(edificioActualizado));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar edificio ID {}: {}", idEdificio, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al actualizar edificio ID {}", idEdificio, e);
            return OperacionResultadoDTO.error("Error al actualizar el edificio");
        }
    }

    @Override
    public OperacionResultadoDTO<Boolean> eliminarEdificio(Long idEdificio) {
        try {
            log.info("Eliminando edificio ID: {}", idEdificio);

            Edificio edificio = edificioRepository.findById(idEdificio)
                    .orElseThrow(() -> new IllegalArgumentException("Edificio no encontrado con ID: " + idEdificio));

            long cantidadPisos = pisoRepository.countByEdificioIdEdificio(idEdificio);
            if (cantidadPisos > 0) {
                log.warn("Intento de eliminar edificio con pisos asociados ID: {}", idEdificio);
                return OperacionResultadoDTO.error(
                        String.format("No se puede eliminar el edificio porque tiene %d piso(s) asociado(s)", cantidadPisos)
                );
            }

            edificioRepository.delete(edificio);
            log.info("Edificio eliminado exitosamente: {}", edificio.getNombre());
            return OperacionResultadoDTO.exito("Edificio eliminado exitosamente", true);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al eliminar edificio ID {}: {}", idEdificio, e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al eliminar edificio ID {}", idEdificio, e);
            return OperacionResultadoDTO.error("Error al eliminar el edificio");
        }
    }

    // MÉTODOS AUXILIARES PRIVADOS

    private Edificio buscarEdificioPorId(Long idEdificio) {
        return edificioRepository.findById(idEdificio)
                .orElseThrow(() -> new IllegalArgumentException("Edificio no encontrado con ID: " + idEdificio));
    }
}