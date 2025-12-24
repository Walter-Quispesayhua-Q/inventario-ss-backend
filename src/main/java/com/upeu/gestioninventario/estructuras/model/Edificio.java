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

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "edificios", schema = "estructuras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE estructuras.edificios SET fecha_eliminacion = NOW() WHERE id_edificio = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Edificio extends Auditable<Usuario> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_edificio")
    private Long idEdificio;
    
    @Column(nullable = false, length = 255)
    private String nombre;
    
    @Column(nullable = false, unique = true, length = 100)
    private String codigo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_estructura", nullable = false)
    private TipoEstructuraEntity tipoEstructura;
    
    @Column(columnDefinition = "TEXT")
    private String direccion;
    
    @Column(name = "numero_pisos")
    private Integer numeroPisos;
    
    @Column(name = "area_total_m2", precision = 10, scale = 2)
    private BigDecimal areaTotalM2;
    
    @Column(name = "ano_construccion")
    private Integer anoConstruccion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable_mantenimiento", nullable = false)
    private Usuario responsableMantenimiento;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @Type(JsonBinaryType.class)
    @Column(name = "propiedades_adicionales", columnDefinition = "jsonb")
    private Map<String, Object> propiedadesAdicionales;
}
