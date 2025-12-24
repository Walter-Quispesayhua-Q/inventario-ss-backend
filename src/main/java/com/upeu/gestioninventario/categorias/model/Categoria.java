package com.upeu.gestioninventario.categorias.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.shared.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


@Entity
@Table(name = "categorias", schema = "inventario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

@SQLDelete(sql = "UPDATE inventario.categorias SET fecha_eliminacion = NOW(), estado = false WHERE id_categoria = ?")
@Where(clause = "fecha_eliminacion IS NULL")


public class Categoria extends Auditable<Usuario> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(name = "nombre_categoria", nullable = false, unique = true)
    private String nombreCategoria;

    @Column(name = "descripcion", nullable = true)
    private String descripcion;

    @Column(name = "icono", nullable = true)
    private  String icono;

    @Column(name = "color", nullable = true)
    private String color;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(name = "visible", nullable = false)
    @Builder.Default
    private Boolean visible = false;

    @Column(name = "es_inteligente", nullable = false)
    @Builder.Default
    private Boolean esInteligente = false;

}
