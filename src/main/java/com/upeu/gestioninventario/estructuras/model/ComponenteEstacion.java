package com.upeu.gestioninventario.estructuras.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.shared.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "componentes_estaciones", schema = "estructuras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE estructuras.componentes_estaciones SET fecha_eliminacion = NOW() WHERE id_componente = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class ComponenteEstacion extends Auditable<Usuario> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_componente")
    private Long idComponente;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "categoria_base", length = 100)
    private String categoriaBase;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column
    @Builder.Default
    private Integer orden = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estacion", nullable = false)
    private Estacion estacion;

    @OneToMany(mappedBy = "componente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<BienEstacion> bienesAsignados = new ArrayList<>();

    public int totalBienes() {
        return bienesAsignados != null ? bienesAsignados.size() : 0;
    }

    public boolean esEquipoCompleto() {
        return "EQUIPO_COMPLETO".equals(tipo);
    }

    public boolean esPeriferico() {
        return "PERIFERICO".equals(tipo);
    }

    public boolean esComponenteSuelto() {
        return "COMPONENTE_SUELTO".equals(tipo);
    }
}
