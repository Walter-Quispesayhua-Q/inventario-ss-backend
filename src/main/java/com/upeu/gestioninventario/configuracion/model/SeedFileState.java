package com.upeu.gestioninventario.configuracion.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "seed_file_state", schema = "configuracion")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeedFileState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre_archivo", nullable = false, unique = true)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "tipo_seed", nullable = false, length = 100)
    private String tipoSeed;

    @Column(name = "hash_contenido", nullable = false, length = 64)
    private String hashContenido;

    @Column(name = "fecha_primera_carga", updatable = false)
    private OffsetDateTime fechaPrimeraCarga;

    @Column(name = "fecha_ultima_carga")
    private OffsetDateTime fechaUltimaCarga;

    @Column(name = "cantidad_recargas")
    @Builder.Default
    private Integer cantidadRecargas = 1;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime ahora = OffsetDateTime.now();
        this.fechaPrimeraCarga = ahora;
        this.fechaUltimaCarga = ahora;
        if (this.cantidadRecargas == null) {
            this.cantidadRecargas = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaUltimaCarga = OffsetDateTime.now();
        this.cantidadRecargas = (this.cantidadRecargas != null ? this.cantidadRecargas : 0) + 1;
    }
}
