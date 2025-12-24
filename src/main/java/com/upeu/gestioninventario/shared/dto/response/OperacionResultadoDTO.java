package com.upeu.gestioninventario.shared.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * DTO genérico para respuestas de operaciones del sistema.
 * <p>
 * Envuelve el resultado de una operación con información de éxito/error,
 * mensaje descriptivo, nivel de notificación y timestamp.
 *
 * @param <T> Tipo del dato retornado en caso de éxito
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperacionResultadoDTO<T> {
    
    private Boolean exito;
    private String mensaje;
    private NivelNotificacion nivel;
    private T data;
    private OffsetDateTime timestamp;

    /**
     * Crea una respuesta exitosa con datos.
     */
    public static <T> OperacionResultadoDTO<T> exito(String mensaje, T data) {
        return new OperacionResultadoDTO<>(
                true,
                mensaje,
                NivelNotificacion.SUCCESS,
                data,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    /**
     * Crea una respuesta exitosa sin datos.
     */
    public static <T> OperacionResultadoDTO<T> exito(String mensaje) {
        return new OperacionResultadoDTO<>(
                true,
                mensaje,
                NivelNotificacion.SUCCESS,
                null,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    /**
     * Crea una respuesta de error.
     */
    public static <T> OperacionResultadoDTO<T> error(String mensaje) {
        return new OperacionResultadoDTO<>(
                false,
                mensaje,
                NivelNotificacion.ERROR,
                null,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    /**
     * Crea una respuesta de advertencia.
     */
    public static <T> OperacionResultadoDTO<T> warning(String mensaje) {
        return new OperacionResultadoDTO<>(
                true,
                mensaje,
                NivelNotificacion.WARNING,
                null,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    /**
     * Crea una respuesta de advertencia con datos.
     */
    public static <T> OperacionResultadoDTO<T> warning(String mensaje, T data) {
        return new OperacionResultadoDTO<>(
                true,
                mensaje,
                NivelNotificacion.WARNING,
                data,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    /**
     * Crea una respuesta con nivel personalizado.
     */
    public static <T> OperacionResultadoDTO<T> custom(String mensaje, NivelNotificacion nivel, T data) {
        return new OperacionResultadoDTO<>(
                nivel == NivelNotificacion.SUCCESS || nivel == NivelNotificacion.INFO,
                mensaje,
                nivel,
                data,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}

