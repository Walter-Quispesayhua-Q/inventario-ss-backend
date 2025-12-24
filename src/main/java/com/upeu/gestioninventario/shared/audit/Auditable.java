package com.upeu.gestioninventario.shared.audit;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Clase base abstracta que proporciona campos de auditoría para todas las entidades del sistema.
 * <p>
 * Implementa soft delete automático y seguimiento de usuarios/fechas de creación y modificación.
 * Las entidades que extiendan esta clase heredarán automáticamente:
 * <ul>
 *   <li>Usuario y fecha de creación</li>
 *   <li>Usuario y fecha de última modificación</li>
 *   <li>Eliminación lógica (soft delete) con campo activo</li>
 * </ul>
 * 
 * @param <U> Tipo de la entidad Usuario para auditoría
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE #{#entityName} SET activo = false, fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("activo = true")
public abstract class Auditable<U> {

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creacion", updatable = false)
    protected U usuarioCreacion;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    protected LocalDateTime fechaCreacion;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_ultima_modificacion")
    protected U usuarioUltimaModificacion;

    @LastModifiedDate
    @Column(name = "fecha_ultima_modificacion", nullable = false)
    protected LocalDateTime fechaUltimaModificacion;

    @Column(name = "fecha_eliminacion")
    protected LocalDateTime fechaEliminacion;

    @Builder.Default
    protected boolean activo = true;
}
