package com.upeu.gestioninventario.ml.repository;

import com.upeu.gestioninventario.ml.model.PlantillaAtributo;
import com.upeu.gestioninventario.ml.model.PlantillaCategoria;
import com.upeu.gestioninventario.categorias.model.TipoAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantillaAtributoRepository extends JpaRepository<PlantillaAtributo, Long> {

    Optional<PlantillaAtributo> findByPlantillaAndTipoAtributo(PlantillaCategoria plantilla, TipoAtributo tipoAtributo);

    @Query("SELECT pa FROM PlantillaAtributo pa JOIN FETCH pa.tipoAtributo JOIN pa.plantilla p WHERE p.nombrePlantilla = :nombre")
    List<PlantillaAtributo> findByPlantillaNombre(@Param("nombre") String nombre);
}
