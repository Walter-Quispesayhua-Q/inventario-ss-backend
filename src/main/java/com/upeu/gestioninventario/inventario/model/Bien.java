package com.upeu.gestioninventario.inventario.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.shared.audit.Auditable;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import com.upeu.gestioninventario.estructuras.model.BienEstacion;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Set;

@Entity
@Table(name = "bienes", schema = "inventario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE inventario.bienes SET fecha_eliminacion = NOW() WHERE id_bien = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Bien extends Auditable<Usuario> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bien")
    private Long id;

    @Column(name = "nombre_bien", nullable = false)
    private String nombreBien;

    @Column(name = "caf", nullable = false)
    private String caf;

    @Column(name = "numero_serie", unique = true)
    private String numeroSerie;

    @Column(name = "estado_fisico", length = 100)
    private String estadoFisico;

    @Column(name = "estado_operacional", length = 100)
    private String estadoOperacional;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion_actual", nullable = false)
    private Ubicacion ubicacionActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable_actual", nullable = false)
    private Persona responsableActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento departamento;

    @OneToMany(mappedBy = "bien", cascade = CascadeType.ALL, 
               fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<BienAtributoValor> atributos;

    @OneToOne(mappedBy = "bien", fetch = FetchType.LAZY)
    @SQLRestriction("fecha_desasignacion IS NULL")
    private BienEstacion asignacionEstacionActiva;
}
