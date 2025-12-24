package com.upeu.gestioninventario.auth.repository;

import com.upeu.gestioninventario.auth.model.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {
    List<UsuarioRol> findByUsuarioId(Long usuarioId);
}