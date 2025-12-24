package com.upeu.gestioninventario.estructuras.service.impl;

import com.upeu.gestioninventario.estructuras.dto.estacion.BienEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.ComponenteEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.ComponenteEstacionInputDTO;
import com.upeu.gestioninventario.estructuras.mapper.ComponenteEstacionMapper;
import com.upeu.gestioninventario.estructuras.model.BienEstacion;
import com.upeu.gestioninventario.estructuras.model.ComponenteEstacion;
import com.upeu.gestioninventario.estructuras.model.Estacion;
import com.upeu.gestioninventario.estructuras.repository.BienEstacionRepository;
import com.upeu.gestioninventario.estructuras.repository.ComponenteEstacionRepository;
import com.upeu.gestioninventario.estructuras.repository.EstacionRepository;
import com.upeu.gestioninventario.estructuras.service.IComponenteService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import com.upeu.gestioninventario.inventario.model.Bien;
import com.upeu.gestioninventario.inventario.repository.BienRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ComponenteServiceImpl implements IComponenteService {

    private final ComponenteEstacionRepository componenteRepository;
    private final EstacionRepository estacionRepository;
    private final BienEstacionRepository bienEstacionRepository;
    private final BienRepository bienRepository;
    private final ComponenteEstacionMapper componenteMapper;

    @Override
    public OperacionResultadoDTO<ComponenteEstacionDTO> crearComponente(ComponenteEstacionInputDTO inputDTO) {
        try {
            log.info("Creando componente '{}' en estación {}", inputDTO.getNombre(), inputDTO.getIdEstacion());

            Estacion estacion = estacionRepository.findById(inputDTO.getIdEstacion())
                    .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada con ID: " + inputDTO.getIdEstacion()));

            if (componenteRepository.existsByEstacionIdEstacionAndNombre(inputDTO.getIdEstacion(), inputDTO.getNombre())) {
                log.warn("Intento de crear componente con nombre duplicado: {} en estación: {}", inputDTO.getNombre(), estacion.getNombre());
                return OperacionResultadoDTO.error("Ya existe un componente con el nombre '" + inputDTO.getNombre() + "' en esta estación");
            }

            Integer orden = inputDTO.getOrden();
            if (orden == null) {
                orden = componenteRepository.findMaxOrdenByEstacion(inputDTO.getIdEstacion()) + 1;
            }

            ComponenteEstacion componente = ComponenteEstacion.builder()
                    .nombre(inputDTO.getNombre())
                    .tipo(inputDTO.getTipo() != null ? inputDTO.getTipo() : "COMPONENTE_SUELTO")
                    .categoriaBase(inputDTO.getCategoriaBase())
                    .descripcion(inputDTO.getDescripcion())
                    .orden(orden)
                    .estacion(estacion)
                    .build();

            ComponenteEstacion guardado = componenteRepository.save(componente);

            if (inputDTO.getIdsBienes() != null && !inputDTO.getIdsBienes().isEmpty()) {
                asignarBienesAComponente(guardado, inputDTO.getIdsBienes(), null);
            }

            log.info("Componente creado exitosamente: {} (ID: {})", guardado.getNombre(), guardado.getIdComponente());
            return OperacionResultadoDTO.exito("Componente creado exitosamente", mapearConBienes(guardado));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear componente: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al crear componente", e);
            return OperacionResultadoDTO.error("Error al crear el componente");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ComponenteEstacionDTO obtenerComponentePorId(Long idComponente) {
        log.info("Obteniendo componente con ID: {}", idComponente);
        ComponenteEstacion componente = componenteRepository.findByIdWithBienes(idComponente)
                .orElseThrow(() -> new RuntimeException("Componente no encontrado: " + idComponente));
        return mapearConBienes(componente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponenteEstacionDTO> listarComponentesPorEstacion(Long idEstacion) {
        log.info("Listando componentes de estacion: {}", idEstacion);
        List<ComponenteEstacion> componentes = componenteRepository.findByEstacionWithBienes(idEstacion);
        return componentes.stream().map(this::mapearConBienes).toList();
    }


    @Override
    public ComponenteEstacionDTO agregarBienAComponente(Long idComponente, Long idBien, Long idUsuario) {
        log.info("Agregando bien {} a componente {}", idBien, idComponente);

        ComponenteEstacion componente = buscarComponentePorId(idComponente);
        Bien bien = bienRepository.findById(idBien)
                .orElseThrow(() -> new RuntimeException("Bien no encontrado: " + idBien));

        BienEstacion bienEstacion = bienEstacionRepository.findByBienIdAndFechaDesasignacionIsNull(idBien)
                .orElseGet(() -> {
                    BienEstacion nueva = BienEstacion.builder()
                            .bien(bien)
                            .estacion(componente.getEstacion())
                            .fechaAsignacion(LocalDateTime.now())
                            .idUsuarioAsignacion(idUsuario)
                            .build();
                    return bienEstacionRepository.save(nueva);
                });

        bienEstacion.setComponente(componente);
        bienEstacionRepository.save(bienEstacion);

        log.info("Bien {} agregado a componente {}", idBien, idComponente);
        return obtenerComponentePorId(idComponente);
    }

    private ComponenteEstacion buscarComponentePorId(Long idComponente) {
        return componenteRepository.findById(idComponente)
                .orElseThrow(() -> new RuntimeException("Componente no encontrado: " + idComponente));
    }



    private void asignarBienesAComponente(ComponenteEstacion componente, List<Long> idsBienes, Long idUsuario) {
        for (Long idBien : idsBienes) {
            try {
                Bien bien = bienRepository.findById(idBien)
                        .orElseThrow(() -> new RuntimeException("Bien no encontrado: " + idBien));

                BienEstacion bienEstacion = bienEstacionRepository.findByBienIdAndFechaDesasignacionIsNull(idBien)
                        .orElseGet(() -> {
                            BienEstacion nueva = BienEstacion.builder()
                                    .bien(bien)
                                    .estacion(componente.getEstacion())
                                    .fechaAsignacion(LocalDateTime.now())
                                    .idUsuarioAsignacion(idUsuario)
                                    .build();
                            return bienEstacionRepository.save(nueva);
                        });

                bienEstacion.setComponente(componente);
                bienEstacionRepository.save(bienEstacion);
            } catch (Exception e) {
                log.warn("Error al asignar bien {} a componente: {}", idBien, e.getMessage());
            }
        }
    }

    private ComponenteEstacionDTO mapearConBienes(ComponenteEstacion componente) {
        ComponenteEstacionDTO dto = componenteMapper.toDTO(componente);

        if (componente.getBienesAsignados() != null) {
            List<BienEstacionDTO> bienesDTO = componente.getBienesAsignados().stream()
                    .filter(be -> be.getFechaDesasignacion() == null)
                    .map(be -> {
                        Bien bien = be.getBien();
                        String marca = extraerAtributo(bien, "Marca");
                        String modelo = extraerAtributo(bien, "Modelo");
                        String responsableNombre = bien.getResponsableActual() != null
                                ? bien.getResponsableActual().getNombre() + " " + bien.getResponsableActual().getApellido()
                                : null;
                        
                        return BienEstacionDTO.builder()
                                .idBien(bien.getId())
                                .nombreBien(bien.getNombreBien())
                                .codigoBien(bien.getCaf())
                                .numeroSerieBien(bien.getNumeroSerie())
                                .categoriaDetectada(bien.getCategoria() != null ? bien.getCategoria().getNombreCategoria() : null)
                                .estadoFisico(bien.getEstadoFisico())
                                .estadoOperacional(bien.getEstadoOperacional())
                                .marca(marca)
                                .modelo(modelo)
                                .responsableActualNombre(responsableNombre)
                                .posicionRelativa(be.getPosicionRelativa())
                                .fechaAsignacion(be.getFechaAsignacion())
                                .build();
                    })
                    .toList();
            dto.setBienes(bienesDTO);
        }

        return dto;
    }
    
    private String extraerAtributo(Bien bien, String nombreAtributo) {
        if (bien.getAtributos() == null) {
            return null;
        }
        return bien.getAtributos().stream()
                .filter(attr -> attr.getTipoAtributo() != null 
                        && nombreAtributo.equalsIgnoreCase(attr.getTipoAtributo().getNombreAtributo()))
                .map(attr -> attr.getValor())
                .findFirst()
                .orElse(null);
    }
}