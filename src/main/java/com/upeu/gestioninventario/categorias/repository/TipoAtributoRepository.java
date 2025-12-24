package com.upeu.gestioninventario.categorias.repository;

import com.upeu.gestioninventario.categorias.model.TipoAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoAtributoRepository extends JpaRepository<TipoAtributo, Long> {
    Optional<TipoAtributo> findByNombreAtributo(String nombreAtributo);

    @Query("SELECT ta.nombreAtributo FROM TipoAtributo ta")
    List<String> findAllNombresAtributo();
}