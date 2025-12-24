package com.upeu.gestioninventario.ubicaciones.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.shared.audit.Auditable;
import com.upeu.gestioninventario.estructuras.model.Estacion;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ubicaciones", schema = "ubicaciones")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

public class Ubicacion extends Auditable<Usuario> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Long id;

    @Column(name = "nombre_ubicacion", nullable = false)
    private String nombreUbicacion;

    @Column(name = "descripcion", nullable = true)
    private String descripcion;

    @Column(name = "edificio")
    private String edificio;

    @Column(name = "piso")
    private String piso;

    @Column(name = "oficina_ambiente")
    private String oficinaAmbiente;

    // Nuevos campos V3.0 - Relación con estructuras jerárquicas
    @Column(name = "id_edificio")
    private Long idEdificio;

    @Column(name = "id_piso")
    private Long idPiso;

    @Column(name = "id_ambiente")
    private Long idAmbiente;

    @Column(name = "id_estacion")
    private Long idEstacion;

    // Relación de solo lectura para obtener datos de la estación (V3.0)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estacion", insertable = false, updatable = false)
    private Estacion estacion;

    @Column(name = "ubicacion_manual", columnDefinition = "TEXT")
    private String ubicacionManual;
}
