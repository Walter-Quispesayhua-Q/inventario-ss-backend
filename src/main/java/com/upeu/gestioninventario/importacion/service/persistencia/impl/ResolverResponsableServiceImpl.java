package com.upeu.gestioninventario.importacion.service.persistencia.impl;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverResponsableService;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.personas.service.IPersonaService;
import com.upeu.gestioninventario.shared.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolverResponsableServiceImpl implements IResolverResponsableService {

    private final IPersonaService personaService;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Usuario> resolverUsuario() {
        log.debug("Resolviendo responsable: Obteniendo usuario actual de la sesion");
        Optional<Usuario> usuarioActual = SecurityUtils.getCurrentAuthenticatedUser();
        usuarioActual.ifPresentOrElse(
                usuario -> log.info("Responsable resuelto: '{}' (ID: {})",
                        usuario.getPersona().getNombre(), usuario.getId()),
                () -> log.error("No se pudo obtener el usuario autenticado")
        );
        return usuarioActual;
    }

    @Override
    @Transactional
    public Persona resolverResponsableDesdeExcel(String nombreResponsableExcel) {
        Usuario usuarioLogueado = resolverUsuario()
                .orElseThrow(() -> new IllegalStateException("No se pudo obtener el usuario actual"));
        
        log.debug("Resolviendo responsable desde Excel: '{}'", nombreResponsableExcel);
        
        Persona persona = personaService.buscarOCrearResponsable(nombreResponsableExcel, usuarioLogueado);
        
        log.info("Responsable resuelto: {} {} (ID: {})", 
                persona.getNombre(), persona.getApellido(), persona.getId());
        
        return persona;
    }
}