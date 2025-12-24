package com.upeu.gestioninventario.estructuras.dto.estacion;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BienEstacionDTO {

    private Long id;

    private Long idBien;
    private String nombreBien;
    private String codigoBien;
    private String numeroSerieBien;

    private Long idEstacion;
    private String nombreEstacion;
    private String codigoEstacion;

    // Información del Ambiente
    private Long idAmbiente;
    private String nombreAmbiente;

    // Información del Piso
    private Long idPiso;
    private String nombrePiso;
    private Integer numeroPiso;

    // Información del Edificio
    private Long idEdificio;
    private String nombreEdificio;

    // Detalles de la asignación
    private String posicionRelativa;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaDesasignacion;
    private Long idUsuarioAsignacion;
    private String nombreUsuarioAsignacion;
    private String observaciones;

    // Categoría detectada desde plantillas en análisis
    private String categoriaDetectada;

    // Grupo al que pertenece el bien
    private String idGrupo;
    private String nombreGrupo;

    // Estado
    private Boolean asignacionActiva;
    
    private String estadoFisico;
    private String estadoOperacional;
    private String marca;
    private String modelo;
    private String responsableActualNombre;

    // Atributos adicionales extraídos
    private Map<String, String> atributosExtraidos;

}