package com.upeu.gestioninventario.auth.service;

import com.upeu.gestioninventario.auth.dto.*;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.util.List;

public interface IAuthService {

    OperacionResultadoDTO<UsuarioDTO> registrarUsuario(UsuarioCreacionInput data);
    
    OperacionResultadoDTO<UsuarioLoginResponseDTO> iniciarSesion(UsuarioLoginInput data);
    
    OperacionResultadoDTO<Boolean> solicitarReseteoPassword(PasswordResetRequestInput request);
    
    OperacionResultadoDTO<Boolean> confirmarReseteoPassword(PasswordResetConfirmInput request);

    OperacionResultadoDTO<Void> cambiarRolUsuario(Long idUsuario, String nuevoRol);

    OperacionResultadoDTO<UsuarioDTO> crearUsuarioDesdePersona(UsuarioDesdePersonaInput data);

    OperacionResultadoDTO<Void> desactivarUsuario(Long idUsuario);

    OperacionResultadoDTO<Void> reactivarUsuario(Long idUsuario);

    List<UsuarioDTO> obtenerUsuariosPorEstado(Boolean activo);

    List<String> obtenerRolesDisponibles();
}
