package com.upeu.gestioninventario.shared.audit;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

/**
 * Implementación de AuditorAware para proporcionar el usuario actual en operaciones de auditoría.
 * <p>
 * Extrae el usuario autenticado del contexto de seguridad de Spring Security.
 * Si hay una transacción activa, usa el usuario del contexto directamente;
 * de lo contrario, recarga el usuario desde la base de datos para obtener el estado actual.
 */
@Component("auditorAware")
@Slf4j
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<Usuario> {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Optional<Usuario> getCurrentAuditor() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (isInvalidAuthentication(authentication)) {
                return Optional.empty();
            }

            Usuario usuarioPrincipal = (Usuario) authentication.getPrincipal();

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                return Optional.of(usuarioPrincipal);
            }

            return recargarUsuarioDesdeBD(usuarioPrincipal);

        } catch (Exception e) {
            log.error("Error obteniendo auditor actual: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private boolean isInvalidAuthentication(Authentication authentication) {
        return authentication == null 
            || !authentication.isAuthenticated() 
            || authentication.getPrincipal() instanceof String;
    }

    private Optional<Usuario> recargarUsuarioDesdeBD(Usuario usuarioPrincipal) {
        try {
            return usuarioRepository.findById(usuarioPrincipal.getId());
        } catch (Exception e) {
            log.warn("Error recargando usuario desde BD, usando instancia del contexto: {}", e.getMessage());
            return Optional.of(usuarioPrincipal);
        }
    }
}