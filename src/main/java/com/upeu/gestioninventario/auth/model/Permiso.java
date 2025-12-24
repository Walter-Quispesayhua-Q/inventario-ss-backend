package com.upeu.gestioninventario.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "permisos", schema = "seguridad")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long id;

    @Column(name = "nombre_permiso", nullable = false, unique = true)
    private String nombrePermiso;

    @Column(name = "descripcion", nullable = true)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
