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
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspecto que intercepta endpoints REST para registrar automáticamente actividades de auditoría.
 * <p>
 * Captura operaciones POST, PUT y DELETE (excepto el endpoint de análisis de importación)
 * para crear registros de auditoría con información del usuario, request y resultado.
 */
//@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaRestAspect {

    private final AuditoriaServiceImpl auditoriaServiceImpl;

    @Around("(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)) && " +
            "!execution(* controller.importacion.com.upeu.gestioninventario.ImportacionController.analizarArchivo(..))")
    public Object auditoriaRestEndpoint(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String nombreMetodo = method.getName();

        log.debug("Interceptando endpoint REST: {}", nombreMetodo);

        String ipAddress = obtenerIpAddress();
        String userAgent = obtenerUserAgent();
        String httpMethod = detectarHttpMethod(method);
        String endpoint = obtenerEndpoint();
        long tiempoInicio = System.currentTimeMillis();

        try {
            Object resultado = joinPoint.proceed();
            registrarAuditoriaExitosa(signature, method, nombreMetodo, resultado, 
                                     httpMethod, endpoint, tiempoInicio, ipAddress, userAgent);
            return resultado;
        } catch (Exception e) {
            registrarAuditoriaFallida(signature, nombreMetodo, e, httpMethod, endpoint, 
                                     ipAddress, userAgent);
            throw e;
        }
    }

    private void registrarAuditoriaExitosa(MethodSignature signature, Method method, String nombreMetodo,
                                          Object resultado, String httpMethod, String endpoint,
                                          long tiempoInicio, String ipAddress, String userAgent) {
        long duracion = System.currentTimeMillis() - tiempoInicio;

        TipoOperacion tipoOperacion = detectarTipoOperacionRest(httpMethod, nombreMetodo);
        TipoEndpoint tipoEndpoint = detectarTipoEndpoint(httpMethod);
        String entidadAfectada = extraerEntidadDeEndpoint(endpoint, nombreMetodo);

        Map<String, Object> metadatos = construirMetadatos(signature, nombreMetodo, httpMethod, endpoint, duracion);
        String descripcion = generarDescripcionRest(tipoOperacion, entidadAfectada, endpoint);
        Usuario usuario = obtenerUsuarioParaAuditoria(resultado);

        auditoriaServiceImpl.registrarActividad(
                tipoOperacion,
                tipoEndpoint,
                entidadAfectada,
                null,
                descripcion,
                metadatos,
                ipAddress,
                userAgent,
                usuario
        );

        log.debug("Auditoría registrada para REST {} {} en {}ms", httpMethod, endpoint, duracion);
    }

    private void registrarAuditoriaFallida(MethodSignature signature, String nombreMetodo, Exception e,
                                          String httpMethod, String endpoint, String ipAddress, String userAgent) {
        log.error("Error en endpoint REST {} {}: {}", httpMethod, endpoint, e.getMessage());

        Map<String, Object> metadatos = construirMetadatos(signature, nombreMetodo, httpMethod, endpoint, null);
        metadatos.put("error", e.getMessage());

        Usuario usuario = auditoriaServiceImpl.obtenerUsuarioActual();

        auditoriaServiceImpl.registrarActividad(
                TipoOperacion.CONSULTAR,
                TipoEndpoint.REST_POST,
                "Sistema",
                null,
                "Error en operación REST: " + endpoint + " - " + e.getMessage(),
                metadatos,
                ipAddress,
                userAgent,
                usuario
        );
    }

    private Usuario obtenerUsuarioParaAuditoria(Object resultado) {
        Usuario usuario = auditoriaServiceImpl.obtenerUsuarioActual();

        if (usuario == null) {
            Long usuarioId = extraerUsuarioIdDelResultado(resultado);
            if (usuarioId != null) {
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

            Object id = resultado.getClass().getMethod("id").invoke(resultado);
            return convertirALong(id);
        } catch (Exception e) {
            log.trace("No se pudo extraer usuario ID del resultado REST: {}", e.getMessage());
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

    private Map<String, Object> construirMetadatos(MethodSignature signature, String nombreMetodo,
                                                   String httpMethod, String endpoint, Long duracion) {
        Map<String, Object> metadatos = new HashMap<>();
        metadatos.put("metodo", nombreMetodo);
        metadatos.put("httpMethod", httpMethod);
        metadatos.put("endpoint", endpoint);
        metadatos.put("clase", signature.getDeclaringTypeName());
        if (duracion != null) {
            metadatos.put("duracionMs", duracion);
        }
        return metadatos;
    }

    private String detectarHttpMethod(Method method) {
        if (method.isAnnotationPresent(PostMapping.class)) return "POST";
        if (method.isAnnotationPresent(PutMapping.class)) return "PUT";
        if (method.isAnnotationPresent(DeleteMapping.class)) return "DELETE";
        if (method.isAnnotationPresent(GetMapping.class)) return "GET";
        return "UNKNOWN";
    }

    private TipoEndpoint detectarTipoEndpoint(String httpMethod) {
        return switch (httpMethod) {
            case "POST" -> TipoEndpoint.REST_POST;
            case "PUT" -> TipoEndpoint.REST_PUT;
            case "DELETE" -> TipoEndpoint.REST_DELETE;
            case "GET" -> TipoEndpoint.REST_GET;
            default -> TipoEndpoint.SISTEMA;
        };
    }

    private TipoOperacion detectarTipoOperacionRest(String httpMethod, String nombreMetodo) {
        String metodoLower = nombreMetodo.toLowerCase();

        if (metodoLower.contains("importar") || metodoLower.contains("import")) {
            return TipoOperacion.IMPORTAR;
        }
        if (metodoLower.contains("exportar") || metodoLower.contains("export")) {
            return TipoOperacion.EXPORTAR;
        }

        return switch (httpMethod) {
            case "POST" -> TipoOperacion.CREAR;
            case "PUT" -> TipoOperacion.ACTUALIZAR;
            case "DELETE" -> TipoOperacion.ELIMINAR;
            default -> TipoOperacion.CONSULTAR;
        };
    }

    private String extraerEntidadDeEndpoint(String endpoint, String nombreMetodo) {
        if (endpoint != null && !endpoint.isEmpty()) {
            String[] partes = endpoint.split("/");
            for (String parte : partes) {
                if (!parte.isEmpty() && !parte.equals("api") && !parte.startsWith("v")) {
                    return parte.substring(0, 1).toUpperCase() + parte.substring(1);
                }
            }
        }

        return nombreMetodo.substring(0, 1).toUpperCase() + nombreMetodo.substring(1);
    }

    private String generarDescripcionRest(TipoOperacion tipoOperacion, String entidad, String endpoint) {
        String accion = switch (tipoOperacion) {
            case CREAR -> "creó datos en";
            case ACTUALIZAR -> "actualizó datos en";
            case ELIMINAR -> "eliminó datos en";
            case IMPORTAR -> "importó datos en";
            case EXPORTAR -> "exportó datos de";
            default -> "realizó operación en";
        };

        return String.format("Usuario %s %s (endpoint: %s)", accion, entidad, endpoint);
    }

    private String obtenerEndpoint() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            log.trace("No se pudo obtener endpoint: {}", e.getMessage());
        }
        return "DESCONOCIDO";
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

    private Long convertirALong(Object id) {
        if (id == null) return null;
        if (id instanceof Long l) return l;
        if (id instanceof Integer i) return i.longValue();
        if (id instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
