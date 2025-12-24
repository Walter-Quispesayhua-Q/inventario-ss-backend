package com.upeu.gestioninventario.categorias.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria_atributos", schema = "inventario",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_categoria", "id_tipo_atributo"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoriaAtributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_atributo", nullable = false)
    private TipoAtributo tipoAtributo;

    @Column(name = "etiqueta", nullable = false)
    private String etiqueta;

    @Column(name = "es_requerido", nullable = false)
    private boolean requerido;

    @Column(name = "valor_defecto")
    private String valorDefecto;

}