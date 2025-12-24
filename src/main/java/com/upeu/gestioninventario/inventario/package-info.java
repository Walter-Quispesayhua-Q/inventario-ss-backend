/**
 * <h1>Módulo: Inventario</h1>
 *
 * <h2>Propósito</h2>
 * Gestión del catálogo de bienes del sistema. Es el módulo central del dominio que representa
 * los activos físicos de la organización con sus atributos dinámicos, categorización,
 * ubicación, responsable y asignación a estaciones de trabajo.
 *
 * <h2>Qué expone</h2>
 * <p><b>GraphQL Mutations:</b></p>
 * <ul>
 *   <li>{@code crearBien} - Crear nuevo bien con campos dinámicos (ADMIN, USER_INTERNO)</li>
 *   <li>{@code actualizarBien} - Actualizar bien existente (ADMIN, USER_INTERNO)</li>
 *   <li>{@code eliminarBien} - Eliminar bien con soft delete (ADMIN, USER_INTERNO)</li>
 * </ul>
 *
 * <p><b>GraphQL Queries:</b></p>
 * <ul>
 *   <li>{@code obtenerBienes} - Consulta paginada con filtros (categoría, ubicación, estado, departamento, responsable)</li>
 *   <li>{@code bienPorId} - Obtener bien por ID con detalles completos</li>
 *   <li>{@code buscarBienes} - Búsqueda por término (nombre, CAF, serie, observaciones)</li>
 *   <li>{@code bienesPorCategoria} - Listar bienes de una categoría</li>
 * </ul>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>Bien</b>: Entidad central con nombre, CAF (código único), número serie, estados físico/operacional</li>
 *   <li><b>BienAtributoValor</b>: Valores de atributos dinámicos vinculados a TipoAtributo de categorías</li>
 *   <li><b>HistorialUbicacion</b>: [PENDIENTE] Registro de movimientos de bienes entre ubicaciones (estructura preparada para futuro)</li>
 *   <li><b>KeyValueInput</b>: Estructura genérica para campos dinámicos en creación/actualización</li>
 *   <li><b>Estados</b>: estadoFisico (EXCELENTE, BUENO, REGULAR, MALO), estadoOperacional (OPERATIVO, EN_REPARACION, etc.)</li>
 * </ul>
 *
 * <h2>Relaciones principales</h2>
 * <ul>
 *   <li>Bien → Categoria (ManyToOne): Clasificación del bien</li>
 *   <li>Bien → Ubicacion (ManyToOne): Ubicación física actual</li>
 *   <li>Bien → Persona (ManyToOne): Responsable actual del bien</li>
 *   <li>Bien → Departamento (ManyToOne): Departamento al que pertenece</li>
 *   <li>Bien → BienAtributoValor (OneToMany): Atributos dinámicos según categoría</li>
 *   <li>Bien → BienEstacion (OneToOne): Asignación a estación de trabajo</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>CAF es único y obligatorio para cada bien</li>
 *   <li>Número de serie es único si se proporciona</li>
 *   <li>Soporta soft delete mediante {@code fechaEliminacion}</li>
 *   <li>Los atributos dinámicos se definen según la categoría del bien</li>
 *   <li>[PENDIENTE] El historial de ubicación se registrará automáticamente en cambios (aún no implementado)</li>
 *   <li>Un bien puede estar asignado a máximo una estación activa</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code shared} (Auditable, OperacionResultadoDTO, PaginacionUtils, GenericSpecifications)</li>
 *   <li><b>Depende de:</b> {@code auth} (Usuario para auditoría)</li>
 *   <li><b>Depende de:</b> {@code categorias} (Categoria, TipoAtributo)</li>
 *   <li><b>Depende de:</b> {@code ubicaciones} (Ubicacion, Departamento)</li>
 *   <li><b>Depende de:</b> {@code personas} (Persona como responsable)</li>
 *   <li><b>Depende de:</b> {@code estructuras} (BienEstacion para asignación a estaciones)</li>
 *   <li><b>Es usado por:</b> {@code dashboard}, {@code importacion}, {@code estructuras}, {@code auditoria}</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>controller</b>: BienController con operaciones GraphQL CRUD</li>
 *   <li><b>dto</b>: Records para bien (BienDTO, BienCreacionInput, BienActualizacionInput, BienesPaginados)</li>
 *   <li><b>dto.bien</b>: DTOs específicos incluyendo atributos dinámicos</li>
 *   <li><b>historial</b>: [PENDIENTE] Subpaquete con model, dto, repository y mapper preparados para HistorialUbicacion (aún no implementado funcionalmente)</li>
 *   <li><b>mapper</b>: MapStruct mappers (BienMapper, BienAtributoValorMapper, AtributoDisplayMapper, HistorialUbicacionMapper)</li>
 *   <li><b>model</b>: Entidades JPA (Bien, BienAtributoValor)</li>
 *   <li><b>repository</b>: BienRepository con queries optimizados y JpaSpecificationExecutor</li>
 *   <li><b>repository.specifications</b>: Specifications JPA para filtros dinámicos</li>
 *   <li><b>service</b>: IBienService, IBienPersistenciaService y sus implementaciones</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.inventario.model.Bien
 * @see com.upeu.gestioninventario.inventario.model.BienAtributoValor
 * @see com.upeu.gestioninventario.inventario.service.IBienService
 * @see com.upeu.gestioninventario.inventario.historial.model.HistorialUbicacion
 */
package com.upeu.gestioninventario.inventario;
