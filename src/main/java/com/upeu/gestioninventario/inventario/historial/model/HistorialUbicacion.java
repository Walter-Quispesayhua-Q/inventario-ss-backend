package com.upeu.gestioninventario.inventario.historial.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.inventario.model.Bien;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_ubicaciones", schema = "inventario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class HistorialUbicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bien", nullable = false)
    private Bien bien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion", nullable = false)
    private Ubicacion ubicacion;

    @Column(name = "fecha_entrada", nullable = false, updatable = false)
    private LocalDateTime fechaEntrada;

    @Column(name = "fecha_salida", nullable = true)
    private LocalDateTime fechaSalida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_registro", nullable = false)
    private Usuario usuarioRegistro;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaEntrada = LocalDateTime.now();
        this.fechaRegistro = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.fechaSalida = LocalDateTime.now();
    }
}
