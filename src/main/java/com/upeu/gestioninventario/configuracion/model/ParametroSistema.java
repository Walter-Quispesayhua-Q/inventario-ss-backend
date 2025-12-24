package com.upeu.gestioninventario.configuracion.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "parametros_sistema", schema = "configuracion")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ParametroSistema {

    @Id
    @Column(name = "clave", nullable = false, unique = true)
    private String clave;

    @Column(name = "valor", nullable = false)
    private String valor;

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
