package com.upeu.gestioninventario.importacion.service.persistencia;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.personas.model.Persona;

import java.util.Optional;

public interface IResolverResponsableService {

    Optional<Usuario> resolverUsuario();
    
    Persona resolverResponsableDesdeExcel(String nombreResponsableExcel);
}