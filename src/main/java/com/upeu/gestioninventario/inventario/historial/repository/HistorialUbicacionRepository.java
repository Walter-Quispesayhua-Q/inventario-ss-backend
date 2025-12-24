package com.upeu.gestioninventario.inventario.historial.repository;

import com.upeu.gestioninventario.inventario.historial.model.HistorialUbicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialUbicacionRepository extends JpaRepository<HistorialUbicacion, Long> {}