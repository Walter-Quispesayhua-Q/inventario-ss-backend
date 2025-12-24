/**
 * <h1>Módulo: Auditoría</h1>
 *
 * <h2>Propósito</h2>
 * Sistema de auditoría que registra automáticamente todas las operaciones del sistema
 * (CRUD, autenticación, importación/exportación) con información del usuario, timestamp,
 * dirección IP y metadatos adicionales en formato JSON.
 *
 * <h2>Qué expone</h2>
 * <p><b>GraphQL Mutations:</b></p>
 * <ul>
 *   <li>{@code crearRegistroActividad} - Crear registro de actividad manual</li>
 * </ul>
 *
 * <p><b>GraphQL Queries:</b></p>
 * <ul>
 *   <li>{@code obtenerRegistrosActividad} - Consulta paginada con filtros (usuario, fecha, tipo, entidad)</li>
 *   <li>{@code registroActividadPorId} - Obtener registro por ID</li>
 *   <li>{@code actividadesPorUsuario} - Historial de actividades por usuario</li>
 *   <li>{@code actividadesPorEntidad} - Historial de actividades por entidad</li>
 * </ul>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>RegistroActividad</b>: Entidad principal que almacena operación, endpoint, entidad afectada,
 *       descripción, metadatos JSONB, IP, user-agent y usuario</li>
 *   <li><b>TipoOperacion</b>: CREAR, ACTUALIZAR, ELIMINAR, LOGIN, LOGOUT, CONSULTAR,
 *       IMPORTAR, EXPORTAR, CAMBIO_PERMISO, CAMBIO_ROL, RESET_PASSWORD</li>
 *   <li><b>TipoEndpoint</b>: GRAPHQL_MUTATION, GRAPHQL_QUERY, REST_POST, REST_GET,
 *       REST_PUT, REST_DELETE, SISTEMA</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>Todo registro tiene fecha de operación en UTC</li>
 *   <li>Soporta soft delete mediante campo {@code fechaEliminacion}</li>
 *   <li>El registro automático se realiza vía AOP (aspectos en {@code shared.audit.aspects})</li>
 *   <li>Usuario puede ser nulo para operaciones del sistema</li>
 *   <li>Metadatos almacenados como JSONB en PostgreSQL</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code shared} (Auditable, PaginacionInfo, PaginacionUtils, GenericSpecifications)</li>
 *   <li><b>Depende de:</b> {@code auth} (Usuario, UsuarioRepository)</li>
 *   <li><b>Depende de:</b> {@code inventario} (Bien - para resolver nombres de entidad en mapper)</li>
 *   <li><b>No debe depender de:</b> otros dominios directamente</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>controller</b>: Controlador GraphQL para exponer API de auditoría</li>
 *   <li><b>dto</b>: Records para input/output (RegistroActividadDTO, RegistrosActividadPaginados)</li>
 *   <li><b>mapper</b>: MapStruct mapper para conversión Entity ↔ DTO</li>
 *   <li><b>model</b>: Entidad RegistroActividad y enums TipoOperacion/TipoEndpoint</li>
 *   <li><b>repository</b>: JpaRepository con queries personalizados</li>
 *   <li><b>repository.specifications</b>: Specifications JPA para filtros dinámicos</li>
 *   <li><b>service</b>: Lógica de negocio para registrar y consultar actividades</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.shared.audit.Auditable
 * @see com.upeu.gestioninventario.shared.audit.aspects.AuditoriaGraphQLAspect
 * @see com.upeu.gestioninventario.shared.audit.aspects.AuditoriaRestAspect
 */
package com.upeu.gestioninventario.auditoria;
