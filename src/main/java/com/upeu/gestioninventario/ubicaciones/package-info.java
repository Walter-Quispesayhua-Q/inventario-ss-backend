/**
 * <h1>Módulo: Ubicaciones</h1>
 *
 * <h2>Propósito</h2>
 * Gestión de ubicaciones físicas y departamentos organizacionales del sistema. Las ubicaciones
 * representan dónde se encuentran los bienes (legacy: edificio/piso/oficina o V3.0: vinculación
 * a estructuras jerárquicas). Los departamentos son unidades organizacionales a las que pertenecen
 * usuarios y bienes.
 *
 * <h2>Qué expone</h2>
 * <p><b>GraphQL Queries - Ubicaciones:</b></p>
 * <ul>
 *   <li>{@code ubicaciones} - Listar ubicaciones sin estación asignada (ubicaciones manuales)</li>
 *   <li>{@code ubicacionPorId} - Obtener ubicación por ID</li>
 * </ul>
 *
 * <p><b>GraphQL Queries - Departamentos:</b></p>
 * <ul>
 *   <li>{@code departamentosDisponibles} - Lista simplificada para dropdowns</li>
 * </ul>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>Ubicacion</b>: Lugar físico con nombre, descripción y campos legacy (edificio/piso/oficina)</li>
 *   <li><b>Ubicación detallada (legacy)</b>: edificio + piso + oficinaAmbiente concatenados</li>
 *   <li><b>Ubicación simple</b>: Solo nombreUbicacion</li>
 *   <li><b>Vinculación V3.0</b>: idEdificio, idPiso, idAmbiente, idEstacion para estructuras jerárquicas</li>
 *   <li><b>Departamento</b>: Unidad organizacional con nombre y descripción</li>
 * </ul>
 *
 * <h2>Modos de ubicación</h2>
 * <ul>
 *   <li><b>Manual (legacy):</b> Ubicación sin idEstacion, con campos edificio/piso/oficina</li>
 *   <li><b>Vinculada a estación (V3.0):</b> Ubicación con idEstacion, relacionada a estructuras</li>
 *   <li>{@code ubicaciones} solo retorna ubicaciones donde idEstacion IS NULL</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>Nombre de departamento es único</li>
 *   <li>UbicacionService crea ubicación solo si no existe una con mismos campos</li>
 *   <li>Ubicación puede ser simple (solo nombre) o detallada (edificio/piso/oficina)</li>
 *   <li>Ubicación extiende Auditable con soft delete</li>
 *   <li>Los bienes referencian Ubicacion para saber dónde están físicamente</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code shared} (Auditable)</li>
 *   <li><b>Depende de:</b> {@code auth} (Usuario para auditoría)</li>
 *   <li><b>Depende de:</b> {@code estructuras} (Estacion para vinculación V3.0, solo lectura)</li>
 *   <li><b>Es usado por:</b> {@code inventario} (Bien tiene ubicacionActual)</li>
 *   <li><b>Es usado por:</b> {@code auth} (Usuario pertenece a Departamento)</li>
 *   <li><b>Es usado por:</b> {@code estructuras} (Ambiente puede vincular a Departamento)</li>
 *   <li><b>Es usado por:</b> {@code importacion} (resolución de ubicaciones)</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>controller</b>: UbicacionController y DepartamentoController con queries GraphQL</li>
 *   <li><b>dto</b>: Records (UbicacionDTO, UbicacionCreacionInput, DepartamentoDTO, DepartamentoSimpleDTO, etc.)</li>
 *   <li><b>mapper</b>: UbicacionMapper y DepartamentoMapper con MapStruct</li>
 *   <li><b>model</b>: Entidades JPA (Ubicacion, Departamento)</li>
 *   <li><b>repository</b>: Repositorios JPA con queries por nombre, estación, ambiente</li>
 *   <li><b>service</b>: UbicacionService y DepartamentoService</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.ubicaciones.model.Ubicacion
 * @see com.upeu.gestioninventario.ubicaciones.model.Departamento
 * @see com.upeu.gestioninventario.ubicaciones.service.UbicacionService
 */
package com.upeu.gestioninventario.ubicaciones;
