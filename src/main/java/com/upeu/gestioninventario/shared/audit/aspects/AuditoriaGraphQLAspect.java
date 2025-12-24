package com.upeu.gestioninventario.shared.audit.aspects;

import com.upeu.gestioninventario.auditoria.model.TipoEndpoint;
import com.upeu.gestioninventario.auditoria.model.TipoOperacion;
import com.upeu.gestioninventario.auditoria.service.AuditoriaServiceImpl;
import com.upeu.gestioninventario.auth.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Aspecto que intercepta mutaciones GraphQL para registrar automáticamente actividades de auditoría.
 * <p>
 * Captura información sobre la operación, usuario, metadatos de la solicitud y resultado
 * para crear un registro completo de auditoría del sistema.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaGraphQLAspect {

    private final AuditoriaServiceImpl auditoriaServiceImpl;

    @Around("@annotation(org.springframework.graphql.data.method.annotation.MutationMapping)")
    public Object auditoriaGraphQLMutation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String nombreMetodo = signature.getName();

        log.debug("Interceptando mutación GraphQL: {}", nombreMetodo);

        String ipAddress = obtenerIpAddress();
        String userAgent = obtenerUserAgent();
        long tiempoInicio = System.currentTimeMillis();

        try {
            Object resultado = joinPoint.proceed();
            registrarAuditoriaExitosa(signature, nombreMetodo, resultado, tiempoInicio, ipAddress, userAgent);
            return resultado;
        } catch (Exception e) {
            registrarAuditoriaFallida(signature, nombreMetodo, e, ipAddress, userAgent);
            throw e;
        }
    }

    private void registrarAuditoriaExitosa(MethodSignature signature, String nombreMetodo, 
                                 Object resultado, long tiempoInicio, 
                                 String ipAddress, String userAgent) {
        long duracion = System.currentTimeMillis() - tiempoInicio;

        TipoOperacion tipoOperacion = detectarTipoOperacion(nombreMetodo);
        String entidadAfectada =extraerEntidad(nombreMetodo);
        Long entidadId = extraerEntidadId(resultado);

        Map<String, Object> metadatos = construirMetadatos(signature, nombreMetodo, duracion);
        String descripcion = generarDescripcion(tipoOperacion, entidadAfectada, entidadId);
        Usuario usuario = obtenerUsuarioParaAuditoria(resultado, nombreMetodo);

        auditoriaServiceImpl.registrarActividad(
                tipoOperacion,
                TipoEndpoint.GRAPHQL_MUTATION,
                entidadAfectada,
                entidadId,
                descripcion,
                metadatos,
                ipAddress,
                userAgent,
                usuario
        );

        log.debug("Auditoría registrada para mutación: {} en {}ms", nombreMetodo, duracion);
    }

    private void registrarAuditoriaFallida(MethodSignature signature, String nombreMetodo,
                                          Exception e, String ipAddress, String userAgent) {
        log.error("Error en mutación GraphQL {}: {}", nombreMetodo, e.getMessage());

        Map<String, Object> metadatos = construirMetadatos(signature, nombreMetodo, null);
        metadatos.put("error", e.getMessage());

        Usuario usuario = auditoriaServiceImpl.obtenerUsuarioActual();

        auditoriaServiceImpl.registrarActividad(
                TipoOperacion.CONSULTAR,
                TipoEndpoint.GRAPHQL_MUTATION,
                extraerEntidad(nombreMetodo),
                null,
                "Error en operación: " + nombreMetodo + " - " + e.getMessage(),
                metadatos,
                ipAddress,
                userAgent,
                usuario
        );
    }

    private Usuario obtenerUsuarioParaAuditoria(Object resultado, String nombreMetodo) {
        Usuario usuario = auditoriaServiceImpl.obtenerUsuarioActual();

        if (usuario == null) {
            Long usuarioId = extraerUsuarioIdDelResultado(resultado);
            if (usuarioId != null) {
                log.debug("Usuario extraído del resultado para auditoría: ID {}", usuarioId);
                usuario = auditoriaServiceImpl.obtenerUsuarioPorId(usuarioId);
            }
        }

        return usuario;
    }

    private Long extraerUsuarioIdDelResultado(Object resultado) {
        if (resultado == null) {
            return null;
        }

        try {
            Object data = extraerDataDelResultado(resultado);
            if (data != null) {
                Object id = data.getClass().getMethod("id").invoke(data);
                return convertirALong(id);
            }
        } catch (Exception e) {
            log.trace("No se pudo extraer usuario ID del resultado: {}", e.getMessage());
        }

        return null;
    }

    private Map<String, Object> construirMetadatos(MethodSignature signature, String nombreMetodo, Long duracion) {
        Map<String, Object> metadatos = new HashMap<>();
        metadatos.put("metodo", nombreMetodo);
        metadatos.put("clase", signature.getDeclaringTypeName());
        if (duracion != null) {
            metadatos.put("duracionMs", duracion);
        }
        return metadatos;
    }

    private TipoOperacion detectarTipoOperacion(String nombreMetodo) {
        String metodoLower = nombreMetodo.toLowerCase();

        if (metodoLower.startsWith("crear") || metodoLower.startsWith("registrar")) {
            return TipoOperacion.CREAR;
        }
        if (metodoLower.startsWith("actualizar") || metodoLower.startsWith("modificar") || metodoLower.startsWith("editar")) {
            return TipoOperacion.ACTUALIZAR;
        }
        if (metodoLower.startsWith("eliminar") || metodoLower.startsWith("borrar") || metodoLower.startsWith("delete")) {
            return TipoOperacion.ELIMINAR;
        }
        if (metodoLower.contains("login") || metodoLower.contains("iniciarsesion")) {
            return TipoOperacion.LOGIN;
        }
        if (metodoLower.contains("logout") || metodoLower.contains("cerrarsesion")) {
            return TipoOperacion.LOGOUT;
        }
        if (metodoLower.contains("resetpassword") || metodoLower.contains("reset")) {
            return TipoOperacion.RESET_PASSWORD;
        }
        if (metodoLower.contains("importar") || metodoLower.contains("import")) {
            return TipoOperacion.IMPORTAR;
        }
        if (metodoLower.contains("exportar") || metodoLower.contains("export")) {
            return TipoOperacion.EXPORTAR;
        }

        return TipoOperacion.CONSULTAR;
    }

    private String extraerEntidad(String nombreMetodo) {
        String entidad = nombreMetodo
                .replaceAll("^(crear|actualizar|eliminar|modificar|editar|registrar|obtener|buscar)", "")
                .replaceAll("(Mutation|Query|Input|DTO)$", "");

        if (entidad.isEmpty()) {
            return "Sistema";
        }

        return entidad.substring(0, 1).toUpperCase() + entidad.substring(1);
    }

    private Long extraerEntidadId(Object resultado) {
        if (resultado == null) {
            return null;
        }

        try {
            Object data = extraerDataDelResultado(resultado);
            if (data != null) {
                Object id = data.getClass().getMethod("id").invoke(data);
                return convertirALong(id);
            }

            Object id = resultado.getClass().getMethod("id").invoke(resultado);
            return convertirALong(id);

        } catch (Exception e) {
            log.trace("No se pudo extraer ID del resultado de tipo {}: {}",
                    resultado.getClass().getSimpleName(), e.getMessage());
        }

        return null;
    }

    private Object extraerDataDelResultado(Object resultado) {
        try {
            var dataMethod = resultado.getClass().getMethod("data");
            return dataMethod.invoke(resultado);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception e) {
            log.trace("Error extrayendo data: {}", e.getMessage());
            return null;
        }
    }

    private Long convertirALong(Object id) {
        if (id == null) {
            return null;
        }
        if (id instanceof Long l) {
            return l;
        }
        if (id instanceof Integer i) {
            return i.longValue();
        }
        if (id instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String generarDescripcion(TipoOperacion tipoOperacion, String entidad, Long entidadId) {
        String accion = switch (tipoOperacion) {
            case CREAR -> "creó";
            case ACTUALIZAR -> "actualizó";
            case ELIMINAR -> "eliminó";
            case LOGIN -> "inició sesión en";
            case LOGOUT -> "cerró sesión en";
            case IMPORTAR -> "importó datos en";
            case EXPORTAR -> "exportó datos de";
            case RESET_PASSWORD -> "restableció contraseña en";
            default -> "realizó operación en";
        };

        if (entidadId != null) {
            return String.format("Usuario %s %s con ID: %d", accion, entidad, entidadId);
        }

        return String.format("Usuario %s %s", accion, entidad);
    }

    private String obtenerIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.trace("No se pudo obtener IP address: {}", e.getMessage());
        }
        return "DESCONOCIDO";
    }

    private String obtenerUserAgent() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.trace("No se pudo obtener User-Agent: {}", e.getMessage());
        }
        return "DESCONOCIDO";
    }
}
