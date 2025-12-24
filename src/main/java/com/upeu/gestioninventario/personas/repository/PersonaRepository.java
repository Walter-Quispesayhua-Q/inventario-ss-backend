package com.upeu.gestioninventario.personas.repository;

import com.upeu.gestioninventario.personas.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByCodigo(String codigo);

    Optional<Persona> findByEmail(String email);

    List<Persona> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT COUNT(p) FROM Persona p WHERE p.id NOT IN (SELECT u.persona.id FROM Usuario u WHERE u.persona IS NOT NULL)")
    long countPersonasSinUsuario();
}