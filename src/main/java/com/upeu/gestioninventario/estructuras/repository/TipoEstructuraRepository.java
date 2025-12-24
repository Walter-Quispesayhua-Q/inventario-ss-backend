package com.upeu.gestioninventario.estructuras.repository;

import com.upeu.gestioninventario.estructuras.model.NivelEstructura;
import com.upeu.gestioninventario.estructuras.model.TipoEstructuraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoEstructuraRepository extends JpaRepository<TipoEstructuraEntity, Long> {

    Optional<TipoEstructuraEntity> findByCodigo(String codigo);

    List<TipoEstructuraEntity> findByNivelAplicableAndActivoTrue(NivelEstructura nivelAplicable);

}

