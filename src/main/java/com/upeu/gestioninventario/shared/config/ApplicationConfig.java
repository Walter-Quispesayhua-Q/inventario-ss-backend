package com.upeu.gestioninventario.shared.config;

import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de componentes de seguridad y autenticación.
 * <p>
 * Define los beans necesarios para el sistema de autenticación de Spring Security,
 * incluyendo UserDetailsService, PasswordEncoder y AuthenticationProvider.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UsuarioRepository usuarioRepository;

    /**
     * Configura el servicio de carga de usuarios con eager loading de roles.
     * Previene LazyInitializationException al cargar roles junto con el usuario.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository.findByEmailWithRoles(username)
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido para usuario: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado con el email: " + username);
                });
    }

    /**
     * Configura el encoder de contraseñas con BCrypt strength factor 12.
     * Factor 12 proporciona mayor seguridad que el default (10) con costo computacional aceptable.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configura el proveedor de autenticación DAO para validación de credenciales.
     * Oculta detalles de "usuario no encontrado" para prevenir enumeración de usuarios.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, 
                                                          PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(true);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}