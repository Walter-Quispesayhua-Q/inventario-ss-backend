/**
 * <h1>Módulo: Personas</h1>
 *
 * <h2>Propósito</h2>
 * Gestión de personas físicas del sistema que pueden ser responsables de bienes,
 * responsables de estaciones de trabajo, o estar vinculadas a usuarios del sistema.
 * Representa individuos independientes del sistema de autenticación.
 *
 * <h2>Qué expone</h2>
 * <p><b>GraphQL Queries (ADMIN, USER_INTERNO):</b></p>
 * <ul>
 *   <li>{@code personas} - Listar todas las personas</li>
 *   <li>{@code personaPorId} - Obtener persona por ID</li>
 *   <li>{@code personaPorCodigo} - Obtener persona por código único</li>
 *   <li>{@code personaConBienes} - Obtener persona con lista de bienes a cargo</li>
 *   <li>{@code personasSinUsuario} - Listar personas no vinculadas a usuarios</li>
 * </ul>
 *
 * <p><b>GraphQL Mutations (solo ADMIN):</b></p>
 * <ul>
 *   <li>{@code crearPersona} - Crear nueva persona</li>
 *   <li>{@code actualizarPersona} - Actualizar persona existente</li>
 *   <li>{@code eliminarPersona} - Eliminar persona (soft delete)</li>
 * </ul>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>Persona</b>: Individuo con nombre, apellido, código único, identificación, email, teléfono</li>
 *   <li><b>Código</b>: Identificador único para la persona (ej: empleado, docente)</li>
 *   <li><b>PersonaConBienesDTO</b>: Vista agregada de persona con bienes a cargo</li>
 *   <li><b>BienResumenDTO</b>: Resumen de bien para listados (id, nombre, CAF, categoría, ubicación)</li>
 *   <li><b>Rol</b>: Campo opcional para clasificar personas (docente, administrativo, etc.)</li>
 * </ul>
 *
 * <h2>Relaciones</h2>
 * <ul>
 *   <li>Usuario → Persona (OneToOne): Usuario del sistema puede tener persona asociada</li>
 *   <li>Bien → Persona (ManyToOne): Persona es responsable actual de bienes</li>
 *   <li>Estacion → Persona (ManyToOne): Persona puede ser responsable de estaciones</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>Código es único y obligatorio</li>
 *   <li>Email es único si se proporciona</li>
 *   <li>Identificación es única si se proporciona</li>
 *   <li>Soporta soft delete mediante {@code fechaEliminacion}</li>
 *   <li>Solo ADMIN puede crear, actualizar o eliminar personas</li>
 *   <li>{@code buscarOCrearResponsable} permite crear persona desde importación si no existe</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code shared} (OperacionResultadoDTO)</li>
 *   <li><b>Es usado por:</b> {@code auth} (Usuario vinculado a Persona)</li>
 *   <li><b>Es usado por:</b> {@code inventario} (Bien tiene responsableActual)</li>
 *   <li><b>Es usado por:</b> {@code estructuras} (Estacion tiene responsable)</li>
 *   <li><b>Es usado por:</b> {@code importacion} (buscarOCrearResponsable)</li>
 *   <li><b>Es usado por:</b> {@code dashboard} (conteo de personas)</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>controller</b>: PersonaController con operaciones GraphQL</li>
 *   <li><b>dto</b>: Records (PersonaDTO, PersonaCreacionDTO, PersonaActualizacionDTO, PersonaConBienesDTO, BienResumenDTO)</li>
 *   <li><b>mapper</b>: PersonaMapper con MapStruct</li>
 *   <li><b>model</b>: Entidad JPA Persona</li>
 *   <li><b>repository</b>: PersonaRepository con queries por código, email, búsqueda</li>
 *   <li><b>service</b>: IPersonaService y PersonaServiceImpl</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.personas.model.Persona
 * @see com.upeu.gestioninventario.personas.service.IPersonaService
 * @see com.upeu.gestioninventario.personas.dto.PersonaConBienesDTO
 */
package com.upeu.gestioninventario.personas;
