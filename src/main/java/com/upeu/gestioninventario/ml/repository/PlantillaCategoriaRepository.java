package com.upeu.gestioninventario.ml.repository;

import com.upeu.gestioninventario.ml.model.PlantillaCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlantillaCategoriaRepository extends JpaRepository<PlantillaCategoria, Long> {

    Optional<PlantillaCategoria> findByIdPlantillaSeed(String idPlantillaSeed);

    @Query("SELECT DISTINCT p FROM PlantillaCategoria p " +
           "LEFT JOIN FETCH p.plantillaAtributos pa " +
           "LEFT JOIN FETCH pa.tipoAtributo " +
           "WHERE p.id = :id")
    Optional<PlantillaCategoria> findByIdWithAtributos(@Param("id") Long id);
    
}