/**
 * <h1>Módulo: Auth (Autenticación y Autorización)</h1>
 *
 * <h2>Propósito</h2>
 * Sistema de autenticación y autorización basado en JWT que gestiona usuarios, roles,
 * permisos y sesiones del sistema. Implementa Spring Security con tokens stateless
 * y soporta recuperación de contraseña vía email.
 *
 * <h2>Qué expone</h2>
 * <p><b>GraphQL Mutations:</b></p>
 * <ul>
 *   <li>{@code registrarUsuario} - Registro de nuevos usuarios con rol OBSERVADOR por defecto</li>
 *   <li>{@code loginUser} - Inicio de sesión con generación de tokens JWT</li>
 *   <li>{@code solicitarReseteoPassword} - Solicitar restablecimiento de contraseña</li>
 *   <li>{@code confirmarReseteoPassword} - Confirmar nuevo password con token</li>
 *   <li>{@code cambiarRolUsuario} - Cambiar rol de usuario (solo ADMIN)</li>
 *   <li>{@code crearUsuarioDesdePersona} - Crear usuario desde persona existente (solo ADMIN)</li>
 *   <li>{@code desactivarUsuario} / {@code reactivarUsuario} - Gestión de estado (solo ADMIN)</li>
 * </ul>
 *
 * <p><b>GraphQL Queries:</b></p>
 * <ul>
 *   <li>{@code usuarios} - Listar usuarios activos (ADMIN, USER_INTERNO)</li>
 *   <li>{@code rolesDisponibles} - Obtener roles del sistema (ADMIN, USER_INTERNO)</li>
 *   <li>{@code usuariosPorEstado} - Filtrar usuarios por estado activo/inactivo (solo ADMIN)</li>
 * </ul>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>Usuario</b>: Implementa {@code UserDetails}, vinculado a {@code Persona} y {@code Departamento}</li>
 *   <li><b>Rol</b>: ADMIN, USER_INTERNO, OBSERVADOR - define nivel de acceso</li>
 *   <li><b>Permiso</b>: Acciones específicas asignables a roles</li>
 *   <li><b>UsuarioRol</b>: Relación many-to-many entre usuarios y roles</li>
 *   <li><b>JWT</b>: Access token (24h) y Refresh token (30 días con "remember me")</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>Email es único en el sistema</li>
 *   <li>Código de usuario es único, formato: XX-YYYY###</li>
 *   <li>Usuario desactivado no puede autenticarse ({@code isEnabled() = activo})</li>
 *   <li>No se puede desactivar ni cambiar rol del último ADMIN activo</li>
 *   <li>Nuevo usuario recibe rol OBSERVADOR por defecto</li>
 *   <li>Token de reset expira en 30 minutos</li>
 *   <li>GraphQL está abierto, autorización se aplica con {@code @PreAuthorize}</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code shared} (OperacionResultadoDTO, EmailService, DataInitializer)</li>
 *   <li><b>Depende de:</b> {@code personas} (Persona, PersonaRepository, PersonaMapper)</li>
 *   <li><b>Depende de:</b> {@code ubicaciones} (Departamento, DepartamentoRepository)</li>
 *   <li><b>No debe depender de:</b> otros dominios (inventario, categorias, etc.)</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>config</b>: SecurityConfig (Spring Security), JwtProperties (configuración JWT)</li>
 *   <li><b>controller</b>: Controlador GraphQL para operaciones de autenticación</li>
 *   <li><b>dto</b>: Records para login, registro, respuestas y gestión de usuarios</li>
 *   <li><b>mapper</b>: MapStruct mappers para Usuario, Rol, Permiso</li>
 *   <li><b>model</b>: Entidades JPA (Usuario, Rol, Permiso, UsuarioRol, RolPermiso, PasswordReset)</li>
 *   <li><b>repository</b>: Repositorios JPA con queries optimizados</li>
 *   <li><b>security</b>: JwtAuthenticationFilter para interceptar y validar tokens</li>
 *   <li><b>service</b>: IAuthService, AuthServiceImpl, JwtService</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.auth.model.Usuario
 * @see com.upeu.gestioninventario.auth.service.JwtService
 * @see com.upeu.gestioninventario.auth.config.SecurityConfig
 */
package com.upeu.gestioninventario.auth;
