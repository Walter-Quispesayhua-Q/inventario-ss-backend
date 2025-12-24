package com.upeu.gestioninventario.auth.controller;

import com.upeu.gestioninventario.auth.dto.*;
import com.upeu.gestioninventario.auth.mapper.UsuarioMapper;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.auth.service.IAuthService;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final IAuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public List<UsuarioDTO> usuarios() {
        log.debug("GraphQL Query: usuarios");
        return usuarioRepository.findByActivoTrueWithPersona()
                .stream()
                .map(usuarioMapper::toDto)
                .toList();
    }

    @MutationMapping(value = "registrarUsuario")
    public OperacionResultadoDTO<UsuarioDTO> registrarUsuario(@Argument("usuario") UsuarioCreacionInput dataNewUser) {
        log.info("Recibida solicitud de registro para email: {}", dataNewUser.email());
        return authService.registrarUsuario(dataNewUser);
    }

    @MutationMapping(value = "loginUser")
    public OperacionResultadoDTO<UsuarioLoginResponseDTO> iniciarSesion(@Argument("credenciales") UsuarioLoginInput dataUser) {
        log.info("Intento de inicio de sesión para email: {}", dataUser.email());
        return authService.iniciarSesion(dataUser);
    }

    @MutationMapping(value = "solicitarReseteoPassword")
    public OperacionResultadoDTO<Boolean> solicitarReseteoPassword(@Argument("request") PasswordResetRequestInput userEmail) {
        log.info("Solicitud de reseteo de contraseña para email: {}", userEmail.email());
        return authService.solicitarReseteoPassword(userEmail);
    }

    @MutationMapping(value = "confirmarReseteoPassword")
    public OperacionResultadoDTO<Boolean> confirmarReseteoPassword(@Argument("request") PasswordResetConfirmInput request) {
        log.info("Confirmación de reseteo de contraseña con token");
        return authService.confirmarReseteoPassword(request);
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public List<String> rolesDisponibles() {
        log.debug("GraphQL Query: rolesDisponibles");
        return authService.obtenerRolesDisponibles();
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public OperacionResultadoDTO<Void> cambiarRolUsuario(@Argument Long idUsuario, @Argument String nuevoRol) {
        log.info("GraphQL Mutation: cambiarRolUsuario(usuario={}, rol={})", idUsuario, nuevoRol);
        return authService.cambiarRolUsuario(idUsuario, nuevoRol);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public OperacionResultadoDTO<UsuarioDTO> crearUsuarioDesdePersona(@Argument("input") UsuarioDesdePersonaInput input) {
        log.info("GraphQL Mutation: crearUsuarioDesdePersona(persona={})", input.idPersona());
        return authService.crearUsuarioDesdePersona(input);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public OperacionResultadoDTO<Void> desactivarUsuario(@Argument Long idUsuario) {
        log.info("GraphQL Mutation: desactivarUsuario(usuario={})", idUsuario);
        return authService.desactivarUsuario(idUsuario);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public OperacionResultadoDTO<Void> reactivarUsuario(@Argument Long idUsuario) {
        log.info("GraphQL Mutation: reactivarUsuario(usuario={})", idUsuario);
        return authService.reactivarUsuario(idUsuario);
    }

    @QueryMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UsuarioDTO> usuariosPorEstado(@Argument Boolean activo) {
        log.debug("GraphQL Query: usuariosPorEstado(activo={})", activo);
        return authService.obtenerUsuariosPorEstado(activo);
    }
}


