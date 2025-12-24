package com.upeu.gestioninventario.estructuras.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.shared.audit.Auditable;
import com.upeu.gestioninventario.personas.model.Persona;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "estaciones", schema = "estructuras",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ambiente_codigo", columnNames = {"id_ambiente", "codigo"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE estructuras.estaciones SET fecha_eliminacion = NOW() WHERE id_estacion = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Estacion extends Auditable<Usuario> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estacion")
    private Long idEstacion;
    
    @Column(nullable = false, length = 255)
    private String nombre;
    
    @Column(nullable = false, length = 100)
    private String codigo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ambiente", nullable = false)
    private Ambiente ambiente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_estructura", nullable = false)
    private TipoEstructuraEntity tipoEstructura;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private Persona responsable;
    
    @Column(name = "capacidad_bienes")
    private Integer capacidadBienes;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @Type(JsonBinaryType.class)
    @Column(name = "propiedades_adicionales", columnDefinition = "jsonb")
    private Map<String, Object> propiedadesAdicionales;
}
