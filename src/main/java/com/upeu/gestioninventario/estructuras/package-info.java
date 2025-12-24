/**
 * <h1>Módulo: Estructuras</h1>
 *
 * <h2>Propósito</h2>
 * Gestión de la infraestructura física del campus universitario con jerarquía de cuatro niveles:
 * Edificio → Piso → Ambiente → Estación. Permite la asignación de bienes a estaciones de trabajo
 * y el control de la distribución física del inventario.
 *
 * <h2>Qué expone</h2>
 * <p><b>GraphQL - Edificios:</b></p>
 * <ul>
 *   <li>{@code edificios}, {@code edificioPorCodigo}, {@code edificioConPisos} - Consultas</li>
 *   <li>{@code crearEdificio}, {@code actualizarEdificio}, {@code eliminarEdificio} - Mutaciones (ADMIN, USER_INTERNO)</li>
 *   <li>{@code agregarPisoAEdificio} - Agregar piso a edificio existente</li>
 * </ul>
 *
 * <p><b>GraphQL - Pisos:</b></p>
 * <ul>
 *   <li>{@code pisos}, {@code pisoConAmbientes}, {@code pisoPorEdificioYNumero} - Consultas</li>
 *   <li>{@code actualizarPiso}, {@code eliminarPiso}, {@code agregarAmbienteAPiso} - Mutaciones</li>
 * </ul>
 *
 * <p><b>GraphQL - Ambientes:</b></p>
 * <ul>
 *   <li>{@code ambientes}, {@code ambientesPorDepartamento}, {@code buscarAmbientes} - Consultas</li>
 *   <li>{@code actualizarAmbiente}, {@code eliminarAmbiente}, {@code agregarEstacionAAmbiente} - Mutaciones</li>
 * </ul>
 *
 * <p><b>GraphQL - Estaciones:</b></p>
 * <ul>
 *   <li>{@code estaciones}, {@code estacionConBienes}, {@code bienesDeEstacion} - Consultas</li>
 *   <li>{@code actualizarEstacion}, {@code eliminarEstacion} - Mutaciones</li>
 * </ul>
 *
 * <p><b>GraphQL - Asignación de Bienes:</b></p>
 * <ul>
 *   <li>{@code estacionActualDelBien}, {@code diagnosticarEstadoBien} - Consultas</li>
 *   <li>{@code asignarBienAEstacion}, {@code asignarBienesMasivos} - Asignación individual/masiva</li>
 *   <li>{@code desasignarBienDeEstacion}, {@code desasignarBienesMasivos} - Desasignación</li>
 *   <li>{@code gestionarBienesEstacion} - Gestión combinada agrupar/desagrupar</li>
 * </ul>
 *
 * <p><b>GraphQL - Tipos de Estructura:</b></p>
 * <ul>
 *   <li>{@code tiposEstructura}, {@code tipoEstructuraPorCodigo}, {@code tiposEstructuraPorNivel} - Consultas</li>
 * </ul>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>Edificio</b>: Estructura física principal con código único, dirección, pisos y responsable</li>
 *   <li><b>Piso</b>: Nivel dentro de un edificio con número y nombre</li>
 *   <li><b>Ambiente</b>: Espacio funcional (oficina, aula, laboratorio) vinculado a un Departamento</li>
 *   <li><b>Estacion</b>: Punto de trabajo donde se asignan bienes, con capacidad máxima</li>
 *   <li><b>BienEstacion</b>: Relación de asignación entre bien y estación con fecha y usuario</li>
 *   <li><b>ComponenteEstacion</b>: Agrupación lógica de bienes dentro de una estación</li>
 *   <li><b>TipoEstructura</b>: Catálogo de tipos aplicables a cada nivel (EDIFICIO, PISO, AMBIENTE, ESTACION)</li>
 *   <li><b>NivelEstructura</b>: Enum EDIFICIO, PISO, AMBIENTE, ESTACION</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>Soporta soft delete con {@code fechaEliminacion} en todas las entidades</li>
 *   <li>Eliminación de estructuras requiere confirmación explícita si hay elementos hijos</li>
 *   <li>Un bien solo puede estar asignado a una estación a la vez (constraint UK)</li>
 *   <li>Reasignación de bien requiere flag {@code forzarReasignacion} o falla</li>
 *   <li>Operaciones de asignación registran usuario y fecha automáticamente</li>
 *   <li>Códigos de edificio y estación son únicos dentro de su ámbito</li>
 *   <li>Propiedades adicionales almacenadas como JSONB para extensibilidad</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code shared} (Auditable, OperacionResultadoDTO)</li>
 *   <li><b>Depende de:</b> {@code auth} (Usuario para auditoría y responsables)</li>
 *   <li><b>Depende de:</b> {@code personas} (Persona como responsable de estación)</li>
 *   <li><b>Depende de:</b> {@code ubicaciones} (Departamento vinculado a Ambiente)</li>
 *   <li><b>Depende de:</b> {@code inventario} (Bien para asignación a estaciones)</li>
 *   <li><b>Es usado por:</b> {@code dashboard} (métricas de estructuras)</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>controller</b>: 6 controladores GraphQL (Edificio, Piso, Ambiente, Estacion, Asignacion, TipoEstructura)</li>
 *   <li><b>dto</b>: ~47 DTOs organizados en subpaquetes (edificio, piso, ambiente, estacion, asignacion, config, seed, tipo)</li>
 *   <li><b>mapper</b>: 8 MapStruct mappers</li>
 *   <li><b>model</b>: Entidades JPA (Edificio, Piso, Ambiente, Estacion, BienEstacion, ComponenteEstacion, TipoEstructura)</li>
 *   <li><b>repository</b>: 7 repositorios JPA</li>
 *   <li><b>service</b>: 7 interfaces + 9 implementaciones (IEstructuraCoordinadorService centraliza operaciones)</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.estructuras.model.Edificio
 * @see com.upeu.gestioninventario.estructuras.model.Estacion
 * @see com.upeu.gestioninventario.estructuras.model.BienEstacion
 * @see com.upeu.gestioninventario.estructuras.service.IEstructuraCoordinadorService
 * @see com.upeu.gestioninventario.estructuras.service.IAsignacionEstacionService
 */
package com.upeu.gestioninventario.estructuras;
