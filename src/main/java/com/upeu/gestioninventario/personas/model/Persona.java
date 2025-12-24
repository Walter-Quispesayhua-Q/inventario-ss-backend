package com.upeu.gestioninventario.personas.model;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "personas", schema = "personas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE personas.personas SET fecha_eliminacion = NOW() WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")

public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "identificacion", nullable = true, unique = true)
    private String identificacion;

    @Column(name = "codigo", nullable = false, unique = true)
    private String codigo;

    @Column(name = "email", nullable = true, unique = true)
    private String email;

    @Column(name = "telefono", nullable = true)
    private String telefono;

    @Column(name = "rol", nullable = true)
    private String rol;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultima_modificacion", nullable = false)
    private LocalDateTime fechaUltimaModificacion;

    @Column(name = "fecha_eliminacion", nullable = true)
    private LocalDateTime fechaEliminacion;

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
