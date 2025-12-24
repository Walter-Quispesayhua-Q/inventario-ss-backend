package com.upeu.gestioninventario.ubicaciones.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "departamentos", schema = "ubicaciones")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departamento")
    private Long id;

    @Column(name = "nombre_departamento", nullable = false, unique = true)
    private String nombreDepartamento;

    @Column(name = "descripcion", nullable = true)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultima_modificacion", nullable = false)
    private LocalDateTime fechaUltimaModificacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaModificacion = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

}
