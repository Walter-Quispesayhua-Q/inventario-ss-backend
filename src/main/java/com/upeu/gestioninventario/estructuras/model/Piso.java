package com.upeu.gestioninventario.estructuras.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.shared.audit.Auditable;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "pisos", schema = "estructuras",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_edificio_codigo", columnNames = {"id_edificio", "codigo"}),
        @UniqueConstraint(name = "uk_edificio_numero", columnNames = {"id_edificio", "numero_piso"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE estructuras.pisos SET fecha_eliminacion = NOW() WHERE id_piso = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Piso extends Auditable<Usuario> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_piso")
    private Long idPiso;
    
    @Column(nullable = false, length = 255)
    private String nombre;
    
    @Column(nullable = false, length = 100)
    private String codigo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_edificio", nullable = false)
    private Edificio edificio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_estructura", nullable = false)
    private TipoEstructuraEntity tipoEstructura;
    
    @Column(name = "numero_piso", nullable = false)
    private Integer numeroPiso;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable_mantenimiento", nullable = false)
    private Usuario responsableMantenimiento;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @Type(JsonBinaryType.class)
    @Column(name = "propiedades_adicionales", columnDefinition = "jsonb")
    private Map<String, Object> propiedadesAdicionales;
}
