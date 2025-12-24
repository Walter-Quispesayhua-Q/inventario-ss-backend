package com.upeu.gestioninventario.ml.model;

import com.upeu.gestioninventario.categorias.model.TipoAtributo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "plantilla_atributos", schema = "inventario",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_plantilla", "id_tipo_atributo"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlantillaAtributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plantilla", nullable = false)
    private PlantillaCategoria plantilla;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_atributo", nullable = false)
    private TipoAtributo tipoAtributo;

    @Column(name = "etiqueta", nullable = false)
    private String etiqueta;

    @Column(name = "es_requerido", nullable = false)
    private Boolean esRequerido = false;

    @Column(name = "orden_ui")
    private Integer ordenUI;

    @Column(name = "placeholder")
    private String placeholder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_extraccion", columnDefinition = "jsonb")
    private Map<String, Object> jsonExtraccion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_validacion", columnDefinition = "jsonb")
    private Map<String, Object> jsonValidacion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_opciones", columnDefinition = "jsonb")
    private Map<String, Object> jsonOpciones;
}