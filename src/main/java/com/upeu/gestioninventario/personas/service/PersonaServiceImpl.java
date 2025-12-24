package com.upeu.gestioninventario.personas.service;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.inventario.model.Bien;
import com.upeu.gestioninventario.inventario.repository.BienRepository;
import com.upeu.gestioninventario.personas.dto.*;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.personas.repository.PersonaRepository;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PersonaServiceImpl implements IPersonaService {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final BienRepository bienRepository;

    private static final Pattern PATRON_TILDES = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final List<String> PREFIJOS_A_QUITAR = List.of(
            "ing.", "ing", "dr.", "dr", "lic.", "lic",
            "sr.", "sr", "sra.", "sra", "srta.", "srta",
            "prof.", "prof", "mg.", "mg", "msc.", "msc"
    );

    @Override
    @Transactional(readOnly = true)
    public List<PersonaDTO> obtenerTodas() {
        return personaRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaDTO obtenerPorId(Long id) {
        return personaRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaDTO obtenerPorCodigo(String codigo) {
        return personaRepository.findByCodigo(codigo)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaConBienesDTO obtenerConBienes(Long id) {
        Persona persona = personaRepository.findById(id).orElse(null);
        if (persona == null) return null;

        List<Bien> bienes = bienRepository.findByResponsableActualId(id);
        List<BienResumenDTO> bienesDTO = bienes.stream()
                .map(this::toBienResumen)
                .toList();

        return PersonaConBienesDTO.of(toDTO(persona), bienesDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaDTO> obtenerSinUsuario() {
        List<Long> idsConUsuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getPersona() != null)
                .map(u -> u.getPersona().getId())
                .toList();

        return personaRepository.findAll().stream()
                .filter(p -> !idsConUsuario.contains(p.getId()))
                .map(this::toDTO)
                .toList();
    }

    @Override
    public OperacionResultadoDTO<PersonaDTO> crear(PersonaCreacionDTO dto) {
        try {
            if (personaRepository.findByCodigo(dto.codigo()).isPresent()) {
                return OperacionResultadoDTO.error("Ya existe una persona con el código: " + dto.codigo());
            }

            if (dto.email() != null && personaRepository.findByEmail(dto.email()).isPresent()) {
                return OperacionResultadoDTO.error("Ya existe una persona con el email: " + dto.email());
            }

            Persona persona = Persona.builder()
                    .nombre(dto.nombre())
                    .apellido(dto.apellido())
                    .identificacion(dto.identificacion())
                    .codigo(dto.codigo())
                    .email(dto.email())
                    .telefono(dto.telefono())
                    .build();

            Persona guardada = personaRepository.save(persona);
            log.info("Persona creada: {} {} (ID: {})", guardada.getNombre(), guardada.getApellido(), guardada.getId());
            return OperacionResultadoDTO.exito("Persona creada exitosamente", toDTO(guardada));

        } catch (Exception e) {
            log.error("Error creando persona: {}", e.getMessage());
            return OperacionResultadoDTO.error("Error al crear persona: " + e.getMessage());
        }
    }

    @Override
    public OperacionResultadoDTO<PersonaDTO> actualizar(Long id, PersonaActualizacionDTO dto) {
        try {
            Persona persona = personaRepository.findById(id).orElse(null);
            if (persona == null) {
                return OperacionResultadoDTO.error("Persona no encontrada con ID: " + id);
            }

            if (dto.nombre() != null) persona.setNombre(dto.nombre());
            if (dto.apellido() != null) persona.setApellido(dto.apellido());
            if (dto.identificacion() != null) persona.setIdentificacion(dto.identificacion());
            if (dto.email() != null) persona.setEmail(dto.email());
            if (dto.telefono() != null) persona.setTelefono(dto.telefono());

            Persona actualizada = personaRepository.save(persona);
            log.info("Persona actualizada: ID {}", id);
            return OperacionResultadoDTO.exito("Persona actualizada exitosamente", toDTO(actualizada));

        } catch (Exception e) {
            log.error("Error actualizando persona: {}", e.getMessage());
            return OperacionResultadoDTO.error("Error al actualizar persona: " + e.getMessage());
        }
    }

    @Override
    public OperacionResultadoDTO<Void> eliminar(Long id) {
        try {
            Persona persona = personaRepository.findById(id).orElse(null);
            if (persona == null) {
                return OperacionResultadoDTO.error("Persona no encontrada con ID: " + id);
            }

            long bienesACargo = bienRepository.countByResponsableActualId(id);
            if (bienesACargo > 0) {
                return OperacionResultadoDTO.error(
                        "No se puede eliminar. La persona tiene " + bienesACargo + " bienes a cargo"
                );
            }

            personaRepository.delete(persona);
            log.info("Persona eliminada: ID {}", id);
            return OperacionResultadoDTO.exito("Persona eliminada exitosamente");

        } catch (Exception e) {
            log.error("Error eliminando persona: {}", e.getMessage());
            return OperacionResultadoDTO.error("Error al eliminar persona: " + e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Persona buscarOCrearResponsable(String nombreResponsableExcel, Usuario usuarioLogueado) {
        if (nombreResponsableExcel == null || nombreResponsableExcel.isBlank()) {
            return usuarioLogueado.getPersona();
        }

        String nombreNormalizado = normalizarNombre(nombreResponsableExcel);
        Persona personaUsuario = usuarioLogueado.getPersona();

        if (personaUsuario != null) {
            String nombreUsuarioNorm = normalizarNombre(
                    personaUsuario.getNombre() + " " +
                    (personaUsuario.getApellido() != null ? personaUsuario.getApellido() : "")
            );
            if (nombreNormalizado.equals(nombreUsuarioNorm) ||
                nombreNormalizado.contains(nombreUsuarioNorm) ||
                nombreUsuarioNorm.contains(nombreNormalizado)) {
                return personaUsuario;
            }
        }

        List<Persona> encontradas = personaRepository
                .findByNombreContainingIgnoreCase(extraerPalabraClave(nombreResponsableExcel));

        for (Persona p : encontradas) {
            String nombrePersonaNorm = normalizarNombre(
                    p.getNombre() + " " + (p.getApellido() != null ? p.getApellido() : "")
            );
            if (nombreNormalizado.equals(nombrePersonaNorm) ||
                nombreNormalizado.contains(nombrePersonaNorm) ||
                nombrePersonaNorm.contains(nombreNormalizado)) {
                return p;
            }
        }

        return crearNuevaPersona(nombreResponsableExcel);
    }

    @Override
    public String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return "";

        String resultado = nombre.toLowerCase().trim();
        String normalizado = Normalizer.normalize(resultado, Normalizer.Form.NFD);
        resultado = PATRON_TILDES.matcher(normalizado).replaceAll("");

        for (String prefijo : PREFIJOS_A_QUITAR) {
            if (resultado.startsWith(prefijo + " ")) {
                resultado = resultado.substring(prefijo.length()).trim();
            }
        }

        return resultado.replaceAll("\\s+", " ").trim();
    }

    private PersonaDTO toDTO(Persona p) {
        var usuarioVinculado = usuarioRepository.findByPersonaId(p.getId());
        boolean tieneUsuario = usuarioVinculado.isPresent();
        
        Long usuarioId = null;
        String usuarioEmail = null;
        
        if (tieneUsuario) {
            usuarioId = usuarioVinculado.get().getId();
            usuarioEmail = usuarioVinculado.get().getEmail();
        }

        return new PersonaDTO(
                p.getId(),
                p.getNombre(),
                p.getApellido(),
                p.getIdentificacion(),
                p.getCodigo(),
                p.getEmail(),
                p.getTelefono(),
                p.getFechaCreacion(),
                p.getFechaUltimaModificacion(),
                tieneUsuario,
                usuarioId,
                usuarioEmail
        );
    }

    private BienResumenDTO toBienResumen(Bien b) {
        return new BienResumenDTO(
                b.getId(),
                b.getNombreBien(),
                b.getCaf(),
                b.getCategoria() != null ? b.getCategoria().getNombreCategoria() : null,
                b.getUbicacionActual() != null ? b.getUbicacionActual().getNombreUbicacion() : null
        );
    }

    private String extraerPalabraClave(String nombreCompleto) {
        String[] palabras = normalizarNombre(nombreCompleto).split("\\s+");
        return palabras.length > 0 ? palabras[0] : nombreCompleto;
    }

    private Persona crearNuevaPersona(String nombreCompleto) {
        String[] partes = normalizarNombre(nombreCompleto).split("\\s+");
        String nombre = capitalizar(partes[0]);
        String apellido = partes.length >= 2
                ? capitalizar(String.join(" ", java.util.Arrays.copyOfRange(partes, 1, partes.length)))
                : null;

        Persona nueva = Persona.builder()
                .nombre(nombre)
                .apellido(apellido)
                .codigo("RESP-" + System.currentTimeMillis())
                .build();

        return personaRepository.save(nueva);
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }
}
