package com.upeu.gestioninventario.auth.repository;

import com.upeu.gestioninventario.auth.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByCodigoUsuario(String codigoUsuario);
    Optional<Usuario> findByPasswordResetToken(String passwordResetToken);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.persona LEFT JOIN FETCH u.departamento LEFT JOIN FETCH u.usuarioRoles ur LEFT JOIN FETCH ur.rol WHERE u.email = :email")
    Optional<Usuario> findByEmailWithRoles(@Param("email") String email);

    @Query("SELECT COUNT(ur) > 0 FROM UsuarioRol ur WHERE ur.rol.nombreRol = :nombreRol AND ur.usuario.activo = true")
    boolean existsByRol(@Param("nombreRol") String nombreRol);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.persona LEFT JOIN FETCH u.departamento WHERE u.activo = true ORDER BY u.persona.nombre")
    List<Usuario> findByActivoTrueWithPersona();

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.usuarioRoles ur LEFT JOIN FETCH ur.rol WHERE u.persona.id = :personaId")
    Optional<Usuario> findByPersonaId(@Param("personaId") Long personaId);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.persona LEFT JOIN FETCH u.departamento WHERE u.activo = false ORDER BY u.persona.nombre")
    List<Usuario> findByActivoFalseWithPersona();
}