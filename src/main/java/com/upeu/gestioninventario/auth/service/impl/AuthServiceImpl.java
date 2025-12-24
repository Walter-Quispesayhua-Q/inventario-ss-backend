package com.upeu.gestioninventario.auth.service.impl;

import com.upeu.gestioninventario.auth.dto.*;
import com.upeu.gestioninventario.auth.mapper.UsuarioMapper;
import com.upeu.gestioninventario.auth.model.Rol;
import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.model.UsuarioRol;
import com.upeu.gestioninventario.auth.repository.RolRepository;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.auth.repository.UsuarioRolRepository;
import com.upeu.gestioninventario.auth.service.IAuthService;
import com.upeu.gestioninventario.auth.service.JwtService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import com.upeu.gestioninventario.shared.services.DataInitializer;
import com.upeu.gestioninventario.shared.services.email.EmailService;
import com.upeu.gestioninventario.personas.mapper.PersonaMapper;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.personas.repository.PersonaRepository;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import com.upeu.gestioninventario.ubicaciones.repository.DepartamentoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PersonaMapper personaMapper;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolRepository rolRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final DepartamentoRepository departamentoRepository;

    @Override
    @Transactional
    public OperacionResultadoDTO<UsuarioDTO> registrarUsuario(UsuarioCreacionInput data) {
        try {
            if (usuarioRepository.findByEmail(data.email()).isPresent()) {
                log.warn("Intento de registro con email duplicado: {}", data.email());
                return OperacionResultadoDTO.error("El email ya está registrado en el sistema");
            }

            Departamento departamento = departamentoRepository.findById(data.departamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));

            String codigo = generateUserCode(data.firstName(), data.lastName());

            Persona nuevaPersona = personaMapper.toEntityFromUsuarioCreacion(data);
            nuevaPersona.setCodigo(codigo);
            Persona personaGuardada = personaRepository.save(nuevaPersona);

            Usuario nuevoUsuario = usuarioMapper.toEntity(data);
            nuevoUsuario.setPasswordHash(passwordEncoder.encode(data.password()));
            nuevoUsuario.setCodigoUsuario(codigo);
            nuevoUsuario.setActivo(true);
            nuevoUsuario.setDepartamento(departamento);
            nuevoUsuario.setPersona(personaGuardada);

            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

            Rol rolPorDefecto = rolRepository.findByNombreRol(DataInitializer.ROL_USER_OBSERVADOR)
                    .orElseThrow(() -> new IllegalArgumentException("Rol por defecto 'USER_OBSERVADOR' no encontrado"));

            UsuarioRol nuevoUsuarioRol = UsuarioRol.builder()
                    .usuario(usuarioGuardado)
                    .rol(rolPorDefecto)
                    .build();
            usuarioRolRepository.save(nuevoUsuarioRol);

            log.info("Usuario registrado exitosamente con rol OBSERVADOR: {}", usuarioGuardado.getEmail());

            return OperacionResultadoDTO.exito(
                    "Usuario registrado exitosamente",
                    usuarioMapper.toDto(usuarioGuardado)
            );

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al registrar usuario: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al registrar usuario", e);
            return OperacionResultadoDTO.error("Error interno del sistema al registrar usuario");
        }
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<UsuarioLoginResponseDTO> iniciarSesion(UsuarioLoginInput data) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(data.email(), data.password())
            );

            Usuario usuario = usuarioRepository.findByEmailWithRoles(data.email())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Usuario no encontrado o inactivo. Verifica tus credenciales"));

            String accessToken = jwtService.generateAccessToken(usuario);
            String refreshToken = jwtService.generateRefreshToken(usuario, data.rememberMe());

            UsuarioLoginResponseDTO loginResponse = new UsuarioLoginResponseDTO(
                    usuario.getId(),
                    accessToken,
                    refreshToken,
                    usuario.getEmail(),
                    usuario.getPersona().getNombre(),
                    usuario.getPersona().getApellido(),
                    usuario.getCodigoUsuario(),
                    usuario.getActivo(),
                    usuario.getUsuarioRoles().stream()
                            .map(ur -> ur.getRol().getNombreRol())
                            .collect(Collectors.toList()),
                    usuario.getDepartamento() != null ? usuario.getDepartamento().getNombreDepartamento() : "No Asignado"
            );

            log.info("Inicio de sesión exitoso para usuario: {}", usuario.getEmail());

            return OperacionResultadoDTO.exito(
                    "Inicio de sesión exitoso",
                    loginResponse
            );

        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para email: {}", data.email());
            return OperacionResultadoDTO.error("Credenciales inválidas");
        } catch (Exception e) {
            log.error("Error al iniciar sesión para email: {}", data.email(), e);
            return OperacionResultadoDTO.error("Error interno del sistema al iniciar sesión");
        }
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<Boolean> solicitarReseteoPassword(PasswordResetRequestInput request) {
        try {
            log.info("Solicitud de reseteo de contraseña para email: {}", request.email());

            var usuarioOptional = usuarioRepository.findByEmail(request.email());
            if (usuarioOptional.isEmpty()) {
                log.warn("Usuario no encontrado para reseteo: {}", request.email());
                return OperacionResultadoDTO.exito(
                        "Si el email existe, recibirás instrucciones para restablecer tu contraseña",
                        true
                );
            }

            Usuario usuario = usuarioOptional.get();
            String token = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);

            usuario.setPasswordResetToken(token);
            usuario.setPasswordResetTokenExpiry(expiryDate);
            usuarioRepository.save(usuario);

            String resetLink = "http://localhost:4200/reset-password?token=" + token;

            emailService.sendHtmlEmail(
                    usuario.getEmail(),
                    "Restablece tu Contraseña",
                    "password-reset-template.html",
                    Map.of(
                            "nombreUsuario", usuario.getPersona().getNombre(),
                            "resetLink", resetLink
                    )
            );

            log.info("Correo de reseteo enviado exitosamente a: {}", request.email());

            return OperacionResultadoDTO.exito(
                    "Correo de recuperación enviado exitosamente",
                    true
            );

        } catch (Exception e) {
            log.error("Error al solicitar reseteo de contraseña", e);
            return OperacionResultadoDTO.error("Error al procesar solicitud de reseteo de contraseña");
        }
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<Boolean> confirmarReseteoPassword(PasswordResetConfirmInput request) {
        try {
            log.info("Confirmación de reseteo de contraseña con token");

            Usuario usuario = usuarioRepository.findByPasswordResetToken(request.token())
                    .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado"));

            if (usuario.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
                usuario.setPasswordResetToken(null);
                usuario.setPasswordResetTokenExpiry(null);
                usuarioRepository.save(usuario);
                log.warn("Token expirado para usuario: {}", usuario.getEmail());
                return OperacionResultadoDTO.error("El token de reseteo ha expirado");
            }

            usuario.setPasswordHash(passwordEncoder.encode(request.nuevaPassword()));
            usuario.setPasswordResetToken(null);
            usuario.setPasswordResetTokenExpiry(null);
            usuarioRepository.save(usuario);

            log.info("Contraseña actualizada exitosamente para usuario: {}", usuario.getEmail());

            return OperacionResultadoDTO.exito(
                    "Contraseña restablecida exitosamente",
                    true
            );

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al confirmar reseteo: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error al confirmar reseteo de contraseña", e);
            return OperacionResultadoDTO.error("Error al restablecer la contraseña");
        }
    }

    private String generateUserCode(String firstName, String lastName) {
        if (firstName == null || lastName == null || firstName.isEmpty() || lastName.isEmpty()) {
            throw new IllegalArgumentException("El nombre y apellido son requeridos para generar el código");
        }

        String currentYear = Year.now().toString();
        String initials = firstName.substring(0, 1).toUpperCase() + lastName.substring(0, 1).toUpperCase();
        String code;

        do {
            int randomNumber = ThreadLocalRandom.current().nextInt(100, 999);
            code = initials + "-" + currentYear + randomNumber;
        } while (usuarioRepository.findByCodigoUsuario(code).isPresent());

        return code;
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<Void> cambiarRolUsuario(Long idUsuario, String nuevoRol) {
        try {
            Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
            if (usuario == null) {
                return OperacionResultadoDTO.error("Usuario no encontrado con ID: " + idUsuario);
            }

            Rol rol = rolRepository.findByNombreRol(nuevoRol).orElse(null);
            if (rol == null) {
                return OperacionResultadoDTO.error("Rol no válido: " + nuevoRol + ". Roles disponibles: ADMIN, USER_INTERNO, OBSERVADOR");
            }

            if (esUltimoAdminActivo(usuario) && !"ADMIN".equals(nuevoRol)) {
                return OperacionResultadoDTO.error("No se puede quitar el rol ADMIN al único administrador activo del sistema");
            }

            usuarioRolRepository.findByUsuarioId(idUsuario).forEach(usuarioRolRepository::delete);

            UsuarioRol nuevoUsuarioRol = UsuarioRol.builder()
                    .usuario(usuario)
                    .rol(rol)
                    .build();
            usuarioRolRepository.save(nuevoUsuarioRol);

            log.info("Rol de usuario {} cambiado a {}", usuario.getEmail(), nuevoRol);
            return OperacionResultadoDTO.exito("Rol cambiado exitosamente a " + nuevoRol);

        } catch (Exception e) {
            log.error("Error cambiando rol de usuario: {}", e.getMessage());
            return OperacionResultadoDTO.error("Error al cambiar rol: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<UsuarioDTO> crearUsuarioDesdePersona(UsuarioDesdePersonaInput data) {
        try {
            Persona persona = personaRepository.findById(data.idPersona()).orElse(null);
            if (persona == null) {
                return OperacionResultadoDTO.error("Persona no encontrada con ID: " + data.idPersona());
            }

            if (usuarioRepository.findByPersonaId(data.idPersona()).isPresent()) {
                return OperacionResultadoDTO.error("La persona ya tiene un usuario vinculado");
            }

            if (usuarioRepository.findByEmail(data.email()).isPresent()) {
                return OperacionResultadoDTO.error("El email ya está registrado en el sistema");
            }

            Departamento departamento = obtenerDepartamento(data.departamentoId());

            if (persona.getEmail() == null || persona.getEmail().isBlank()) {
                persona.setEmail(data.email());
                personaRepository.save(persona);
            }

            String codigo = generateUserCode(persona.getNombre(), persona.getApellido());

            Usuario nuevoUsuario = Usuario.builder()
                    .email(data.email())
                    .passwordHash(passwordEncoder.encode(data.password()))
                    .codigoUsuario(codigo)
                    .activo(true)
                    .departamento(departamento)
                    .persona(persona)
                    .build();

            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

            String nombreRol = (data.rolInicial() != null && !data.rolInicial().isBlank())
                    ? data.rolInicial()
                    : DataInitializer.ROL_USER_OBSERVADOR;

            Rol rol = rolRepository.findByNombreRol(nombreRol)
                    .orElseGet(() -> rolRepository.findByNombreRol(DataInitializer.ROL_USER_OBSERVADOR)
                            .orElseThrow(() -> new IllegalArgumentException("Rol por defecto no encontrado")));

            UsuarioRol nuevoUsuarioRol = UsuarioRol.builder()
                    .usuario(usuarioGuardado)
                    .rol(rol)
                    .build();
            usuarioRolRepository.save(nuevoUsuarioRol);

            log.info("Usuario creado desde persona existente: {} para {} {}", 
                    usuarioGuardado.getEmail(), persona.getNombre(), persona.getApellido());

            return OperacionResultadoDTO.exito(
                    "Usuario creado exitosamente para " + persona.getNombre() + " " + persona.getApellido(),
                    usuarioMapper.toDto(usuarioGuardado)
            );

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear usuario desde persona: {}", e.getMessage());
            return OperacionResultadoDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al crear usuario desde persona", e);
            return OperacionResultadoDTO.error("Error interno del sistema");
        }
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<Void> desactivarUsuario(Long idUsuario) {
        try {
            Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
            if (usuario == null) {
                return OperacionResultadoDTO.error("Usuario no encontrado con ID: " + idUsuario);
            }

            if (!usuario.getActivo()) {
                return OperacionResultadoDTO.error("El usuario ya está desactivado");
            }

            if (esUltimoAdminActivo(usuario)) {
                return OperacionResultadoDTO.error("No se puede desactivar al único administrador activo del sistema");
            }

            usuario.setActivo(false);
            usuarioRepository.save(usuario);

            log.info("Usuario desactivado: {}", usuario.getEmail());
            return OperacionResultadoDTO.exito("Usuario desactivado exitosamente");

        } catch (Exception e) {
            log.error("Error desactivando usuario: {}", e.getMessage());
            return OperacionResultadoDTO.error("Error al desactivar usuario: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public OperacionResultadoDTO<Void> reactivarUsuario(Long idUsuario) {
        try {
            Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
            if (usuario == null) {
                return OperacionResultadoDTO.error("Usuario no encontrado con ID: " + idUsuario);
            }

            if (usuario.getActivo()) {
                return OperacionResultadoDTO.error("El usuario ya está activo");
            }

            usuario.setActivo(true);
            usuarioRepository.save(usuario);

            log.info("Usuario reactivado: {}", usuario.getEmail());
            return OperacionResultadoDTO.exito("Usuario reactivado exitosamente");

        } catch (Exception e) {
            log.error("Error reactivando usuario: {}", e.getMessage());
            return OperacionResultadoDTO.error("Error al reactivar usuario: " + e.getMessage());
        }
    }

    @Override
    public List<UsuarioDTO> obtenerUsuariosPorEstado(Boolean activo) {
        List<Usuario> usuarios;
        
        if (activo == null) {
            usuarios = usuarioRepository.findAll();
        } else if (activo) {
            usuarios = usuarioRepository.findByActivoTrueWithPersona();
        } else {
            usuarios = usuarioRepository.findByActivoFalseWithPersona();
        }

        return usuarios.stream()
                .map(usuarioMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<String> obtenerRolesDisponibles() {
        return rolRepository.findAll().stream()
                .map(Rol::getNombreRol)
                .collect(Collectors.toList());
    }

    private boolean esUltimoAdminActivo(Usuario usuario) {
        boolean esAdmin = usuario.getUsuarioRoles().stream()
                .anyMatch(ur -> "ADMIN".equals(ur.getRol().getNombreRol()));
        
        if (!esAdmin) {
            return false;
        }

        long adminsActivos = usuarioRepository.findByActivoTrueWithPersona().stream()
                .filter(u -> u.getUsuarioRoles().stream()
                        .anyMatch(ur -> "ADMIN".equals(ur.getRol().getNombreRol())))
                .count();

        return adminsActivos <= 1;
    }

    private Departamento obtenerDepartamento(Long departamentoId) {
        if (departamentoId != null) {
            return departamentoRepository.findById(departamentoId)
                    .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado con ID: " + departamentoId));
        }
        
        return departamentoRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay departamentos configurados en el sistema"));
    }
}

