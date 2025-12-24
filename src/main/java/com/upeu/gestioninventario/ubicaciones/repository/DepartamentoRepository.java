package com.upeu.gestioninventario.ubicaciones.repository;

import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    Optional<Departamento> findByNombreDepartamento(String nombreDepartamento);
}