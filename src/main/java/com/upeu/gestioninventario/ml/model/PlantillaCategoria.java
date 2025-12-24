package com.upeu.gestioninventario.ml.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "plantillas_categoria", schema = "inventario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlantillaCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plantilla")
    private Long id;

    @Column(name = "id_plantilla_seed", unique = true)
    private String idPlantillaSeed;

    @Column(name = "nombre_plantilla", nullable = false, unique = true)
    private String nombrePlantilla;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "categoria_semantica", length = 150)
    private String categoriaSemantica;

    // Campos JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_scoring", columnDefinition = "jsonb")
    private Map<String, Object> jsonScoring;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_reconocimiento", columnDefinition = "jsonb")
    private Map<String, Object> jsonReconocimiento;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_patrones", columnDefinition = "jsonb")
    private Map<String, Object> jsonPatrones;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_inteligencia", columnDefinition = "jsonb")
    private Map<String, Object> jsonInteligencia;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_aprendizaje", columnDefinition = "jsonb")
    private Map<String, Object> jsonAprendizaje;

    // Auditoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creacion", nullable = false)
    private Usuario usuarioCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_ultima_modificacion")
    private Usuario usuarioUltimaModificacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultima_modificacion", nullable = false)
    private LocalDateTime fechaUltimaModificacion;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @OneToMany(mappedBy = "plantilla", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PlantillaAtributo> plantillaAtributos = new java.util.HashSet<>();


    @PrePersist
    protected void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        this.fechaCreacion = ahora;
        this.fechaUltimaModificacion = ahora;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaUltimaModificacion = LocalDateTime.now();
    }
}