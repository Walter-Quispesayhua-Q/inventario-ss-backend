package com.upeu.gestioninventario.inventario.service;

import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.inventario.dto.bien.BienDTO;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;

import java.util.Map;

public interface IBienPersistenciaService {

    BienDTO guardarBien(Map<String, String> campos, Categoria categoria, Persona responsable, Ubicacion ubicacion, Departamento departamento);

    BienDTO actualizarBienExistente(Long bienId, Map<String, String> campos, Categoria categoria, Ubicacion ubicacion);
}