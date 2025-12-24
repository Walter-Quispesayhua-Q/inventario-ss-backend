package com.upeu.gestioninventario.estructuras.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "tipos_estructura", schema = "estructuras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEstructuraEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Long idTipo;
    
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;
    
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_aplicable", nullable = false, length = 20)
    private NivelEstructura nivelAplicable;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(length = 100)
    private String icono;
    
    @Column(length = 50)
    private String color;
    
    @Type(JsonBinaryType.class)
    @Column(name = "configuracion_campos", columnDefinition = "jsonb")
    private Map<String, Object> configuracionCampos;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_ultima_modificacion")
    private LocalDateTime fechaUltimaModificacion;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaUltimaModificacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaUltimaModificacion = LocalDateTime.now();
    }
}
