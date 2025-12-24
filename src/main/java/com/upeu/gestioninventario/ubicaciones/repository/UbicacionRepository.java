package com.upeu.gestioninventario.ubicaciones.repository;

import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    Optional<Ubicacion> findByEdificioAndPisoAndOficinaAmbiente(String edificio, String piso, String oficinaAmbiente);

    Optional<Ubicacion> findByNombreUbicacion(String nombreUbicacion);

    Optional<Ubicacion> findByIdEstacion(Long idEstacion);

    Optional<Ubicacion> findByNombreUbicacionAndIdEstacionIsNull(String nombreUbicacion);

    Optional<Ubicacion> findByIdAmbiente(Long idAmbiente);

    List<Ubicacion> findAllByIdEstacionIsNull();
}