package com.upeu.gestioninventario.estructuras.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.shared.audit.Auditable;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "ambientes", schema = "estructuras",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_piso_codigo", columnNames = {"id_piso", "codigo"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE estructuras.ambientes SET fecha_eliminacion = NOW() WHERE id_ambiente = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Ambiente extends Auditable<Usuario> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ambiente")
    private Long idAmbiente;
    
    @Column(nullable = false, length = 255)
    private String nombre;
    
    @Column(nullable = false, length = 100)
    private String codigo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_piso", nullable = false)
    private Piso piso;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_estructura", nullable = false)
    private TipoEstructuraEntity tipoEstructura;

    @Column(name = "capacidad_personas")
    private Integer capacidadPersonas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable", nullable = false)
    private Usuario responsable;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento_responsable")
    private Departamento departamentoResponsable;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @Type(JsonBinaryType.class)
    @Column(name = "propiedades_adicionales", columnDefinition = "jsonb")
    private Map<String, Object> propiedadesAdicionales;

    @Column(name = "palabras_clave_ubicacion")
    private String palabrasClaveUbicacion;
}
