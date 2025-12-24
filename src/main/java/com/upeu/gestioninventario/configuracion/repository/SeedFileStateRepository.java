package com.upeu.gestioninventario.configuracion.repository;

import com.upeu.gestioninventario.configuracion.model.SeedFileState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeedFileStateRepository extends JpaRepository<SeedFileState, Long> {

    Optional<SeedFileState> findByNombreArchivo(String nombreArchivo);

}
