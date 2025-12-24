package com.upeu.gestioninventario.auditoria.model;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.shared.audit.Auditable;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Entity
@Table(name = "registros_actividad", schema = "auditoria", indexes = {
        @Index(name = "idx_registro_fecha", columnList = "fecha_operacion"),
        @Index(name = "idx_registro_usuario", columnList = "id_usuario"),
        @Index(name = "idx_registro_tipo_op", columnList = "tipo_operacion"),
        @Index(name = "idx_registro_entidad", columnList = "entidad_afectada")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE auditoria.registros_actividad SET fecha_eliminacion = NOW() WHERE id_registro = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class RegistroActividad extends Auditable<Usuario> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 50)
    private TipoOperacion tipoOperacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_endpoint", nullable = false, length = 50)
    private TipoEndpoint tipoEndpoint;

    @Column(name = "entidad_afectada", length = 100)
    private String entidadAfectada;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Type(JsonType.class)
    @Column(name = "metadatos", columnDefinition = "jsonb")
    private Map<String, Object> metadatos;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "fecha_operacion", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime fechaOperacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @PrePersist
    protected void onCreate() {
        if (this.fechaOperacion == null) {
            this.fechaOperacion = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
}
