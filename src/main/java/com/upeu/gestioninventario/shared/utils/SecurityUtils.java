package com.upeu.gestioninventario.shared.utils;

import com.upeu.gestioninventario.auth.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Utilidades para operaciones relacionadas con seguridad y autenticación.
 * <p>
 * Proporciona métodos para acceder al usuario autenticado del contexto de seguridad.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Clase de utilidades no instanciable");
    }

    /**
     * Obtiene el usuario actualmente autenticado del contexto de seguridad.
     * 
     * @return Optional con el usuario si está autenticado, Optional.empty() en caso contrario
     */
    public static Optional<Usuario> getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || 
            !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        
        return Optional.of((Usuario) authentication.getPrincipal());
    }
}