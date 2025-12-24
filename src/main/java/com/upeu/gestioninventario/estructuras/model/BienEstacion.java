package com.upeu.gestioninventario.estructuras.model;

import com.upeu.gestioninventario.inventario.model.Bien;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bienes_estaciones", schema = "estructuras",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_bien_estacion", columnNames = {"id_bien"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienEstacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bien", nullable = false)
    private Bien bien;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estacion", nullable = false)
    private Estacion estacion;
    
    @Column(name = "posicion_relativa", length = 100)
    private String posicionRelativa;
    
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;
    
    @Column(name = "fecha_desasignacion")
    private LocalDateTime fechaDesasignacion;
    
    @Column(name = "id_usuario_asignacion")
    private Long idUsuarioAsignacion;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_componente")
    private ComponenteEstacion componente;
    
    @PrePersist
    protected void onCreate() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
    }
}
