/**
 * <h1>Módulo: Shared</h1>
 *
 * <h2>Propósito</h2>
 * Infraestructura compartida y transversal del sistema. Contiene clases base, utilidades,
 * configuraciones, DTOs genéricos y servicios de inicialización que son utilizados por
 * todos los demás módulos de la aplicación.
 *
 * <h2>Qué expone</h2>
 * <p><b>Clases base para entidades:</b></p>
 * <ul>
 *   <li>{@code Auditable<U>} - Clase base con auditoría (usuario/fecha creación/modificación), soft delete</li>
 * </ul>
 *
 * <p><b>DTOs genéricos:</b></p>
 * <ul>
 *   <li>{@code OperacionResultadoDTO<T>} - Respuesta estándar con exito, mensaje, nivel, data, timestamp</li>
 *   <li>{@code PaginacionInfo} - Metadatos de paginación (página actual, total páginas, total elementos)</li>
 *   <li>{@code RespuestaPaginada<T>} - Wrapper genérico para respuestas paginadas</li>
 *   <li>{@code NivelNotificacion} - Enum SUCCESS, INFO, WARNING, ERROR</li>
 * </ul>
 *
 * <p><b>Specifications JPA:</b></p>
 * <ul>
 *   <li>{@code GenericSpecifications} - Utilidades para filtros: fieldEquals, fieldLike, searchInFields, fieldBetween, etc.</li>
 * </ul>
 *
 * <p><b>Utilidades:</b></p>
 * <ul>
 *   <li>{@code PaginacionUtils} - Creación de Pageable y PaginacionInfo normalizados</li>
 *   <li>{@code StringUtils} - Manipulación de strings (toUpperSnakeCase, etc.)</li>
 *   <li>{@code SecurityUtils} - Obtención de usuario autenticado</li>
 *   <li>{@code IHashCalculator} - Cálculo de hash SHA-256 para archivos</li>
 *   <li>{@code EstandarizadorNombresService} - Normalización de nombres de categorías</li>
 * </ul>
 *
 * <p><b>Servicios:</b></p>
 * <ul>
 *   <li>{@code DataInitializer} - Inicialización de roles, usuarios admin, departamentos, sincronización de plantillas</li>
 *   <li>{@code CommonAttributeRules} - Definición de campos base comunes a todos los bienes</li>
 *   <li>{@code EmailService} - Servicio de envío de correos (interfaz e implementación)</li>
 * </ul>
 *
 * <p><b>Aspectos de auditoría:</b></p>
 * <ul>
 *   <li>{@code AuditoriaGraphQLAspect} - Logging automático de operaciones GraphQL</li>
 *   <li>{@code AuditoriaRestAspect} - Logging automático de operaciones REST</li>
 *   <li>{@code AuditorAwareImpl} - Proveedor del usuario actual para @CreatedBy/@LastModifiedBy</li>
 * </ul>
 *
 * <h2>Configuraciones</h2>
 * <ul>
 *   <li>{@code ApplicationConfig} - Configuración general de la aplicación</li>
 *   <li>{@code AsyncConfig} - Configuración de tareas asíncronas</li>
 *   <li>{@code GraphqlConfig} - Configuración de GraphQL</li>
 *   <li>{@code JacksonConfig} - Configuración de serialización JSON</li>
 *   <li>{@code WebConfig} - Configuración CORS y MVC</li>
 *   <li>{@code PlantillasProperties} - Configuración externa para plantillas ML</li>
 *   <li>{@code InicializacionProperties} - Configuración de inicialización del sistema</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>Auditable establece fechas automáticamente con @PrePersist/@PreUpdate</li>
 *   <li>Soft delete automático mediante @SQLDelete y @SQLRestriction</li>
 *   <li>Paginación limitada a máximo 100 elementos por página</li>
 *   <li>DataInitializer se ejecuta al inicio (@Order(1)) como CommandLineRunner</li>
 *   <li>Contraseñas por defecto deben cambiarse en producción</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>No depende de:</b> Otros módulos de dominio (es módulo base)</li>
 *   <li><b>Es usado por:</b> TODOS los demás módulos del sistema</li>
 * </ul>
 *
 * <h2>Nota sobre DataInitializer</h2>
 * <p>DataInitializer sí importa elementos de otros módulos (auth, categorias, ml, ubicaciones,
 * personas) para la inicialización de datos base. Esta es una dependencia unidireccional
 * de arranque que no viola la separación de dominios.</p>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>audit</b>: Auditable, AuditorAwareImpl, aspectos de logging</li>
 *   <li><b>config</b>: Configuraciones Spring (Application, Async, GraphQL, Jackson, Web)</li>
 *   <li><b>config.properties</b>: @ConfigurationProperties para configuración externa</li>
 *   <li><b>dto.pagination</b>: PaginacionInfo, RespuestaPaginada</li>
 *   <li><b>dto.response</b>: OperacionResultadoDTO, NivelNotificacion</li>
 *   <li><b>services</b>: DataInitializer, EmailService</li>
 *   <li><b>services.rules</b>: CommonAttributeRules para campos base de bienes</li>
 *   <li><b>specifications</b>: GenericSpecifications para criterios JPA</li>
 *   <li><b>utils</b>: PaginacionUtils, SecurityUtils, StringUtils</li>
 *   <li><b>utils.hash</b>: IHashCalculator y HashCalculatorServiceImpl</li>
 *   <li><b>utils.naming</b>: EstandarizadorNombresService</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.shared.audit.Auditable
 * @see com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO
 * @see com.upeu.gestioninventario.shared.specifications.GenericSpecifications
 * @see com.upeu.gestioninventario.shared.services.DataInitializer
 */
package com.upeu.gestioninventario.shared;
