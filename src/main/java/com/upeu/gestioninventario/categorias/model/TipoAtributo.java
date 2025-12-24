package com.upeu.gestioninventario.categorias.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipos_atributo", schema = "inventario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TipoAtributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_atributo", nullable = false, unique = true)
    private String nombreAtributo;

    @Column(name = "tipo_dato", nullable = false, length = 50)
    private String tipoDato;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
