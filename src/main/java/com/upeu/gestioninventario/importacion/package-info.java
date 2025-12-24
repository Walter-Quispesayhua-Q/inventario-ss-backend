/**
 * <h1>Módulo: Importación</h1>
 *
 * <h2>Propósito</h2>
 * Procesamiento inteligente de archivos Excel/CSV para importación masiva de bienes al inventario.
 * Implementa un flujo de dos fases: análisis (sin persistir) y confirmación (persistencia).
 * Soporta múltiples formatos de archivo (matriz, clave-valor) y detección automática de categorías,
 * responsables, ubicaciones y agrupación de componentes.
 *
 * <h2>Qué expone</h2>
 * <p><b>REST API (no GraphQL):</b></p>
 * <ul>
 *   <li>{@code POST /api/v1/importacion/analizar} - Analiza archivo sin persistir (público)</li>
 *   <li>{@code POST /api/v1/importacion/confirmar} - Ejecuta importación confirmada (ADMIN, USER_INTERNO)</li>
 * </ul>
 *
 * <h2>Flujo de importación</h2>
 * <ol>
 *   <li><b>Análisis:</b> Usuario sube archivo → Sistema detecta formato, categorías, responsables</li>
 *   <li><b>Vista previa:</b> Usuario revisa hojas analizadas, estaciones sugeridas, errores</li>
 *   <li><b>Confirmación:</b> Usuario confirma con sesionId → Sistema persiste bienes/estaciones</li>
 * </ol>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>RespuestaImportacionDTO</b>: Resultado del análisis con hojas, estaciones, estadísticas</li>
 *   <li><b>ImportacionConfirmacionInput</b>: Sesión ID + grupos confirmados por el usuario</li>
 *   <li><b>ImportacionResultadoDTO</b>: Resultado final (creados, actualizados, errores, estaciones)</li>
 *   <li><b>HojaAnalizadaDTO</b>: Análisis de cada hoja del Excel con tipo detectado</li>
 *   <li><b>TipoFormatoDetectado</b>: FORMATO_MATRIZ, FORMATO_CLAVE_VALOR, FORMATO_TABLA</li>
 *   <li><b>SesionAnalisis</b>: Cache temporal de datos analizados para confirmación posterior</li>
 * </ul>
 *
 * <h2>Arquitectura del módulo</h2>
 * <ul>
 *   <li><b>service/analisis</b>: Interfaces para análisis de archivos (IAnalizadorArchivo, IDetectorFormato,
 *       IDetectorCategoria, IExtractorAtributos, IMapeadorDatos, IGeneradorNombresBien)</li>
 *   <li><b>service/analisis/lectores</b>: Lectores específicos por formato (Excel, CSV)</li>
 *   <li><b>service/persistencia</b>: Interfaces para resolución y persistencia (IConfirmacionImportacionService,
 *       IResolverCategoriaService, IResolverResponsableService, IResolverUbicacionService)</li>
 *   <li><b>service/cache</b>: Almacenamiento temporal de sesiones de análisis</li>
 *   <li><b>dto/formato</b>: DTOs organizados por tipo (matriz, clave-valor, común, respuesta)</li>
 *   <li><b>dto/agrupacion</b>: Configuración de agrupación de componentes en estaciones</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>El análisis no modifica la base de datos (solo lectura)</li>
 *   <li>Sesión de análisis tiene tiempo de expiración en caché</li>
 *   <li>La confirmación requiere sesionId válido del análisis previo</li>
 *   <li>Se soportan archivos .xlsx, .xls y .csv</li>
 *   <li>Detección automática de categorías por palabras clave y plantillas ML</li>
 *   <li>Validación de CAF para evitar duplicados en bienes existentes</li>
 *   <li>Creación automática de estaciones si no existen</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code shared} (OperacionResultadoDTO)</li>
 *   <li><b>Depende de:</b> {@code inventario} (Bien, BienRepository para persistencia)</li>
 *   <li><b>Depende de:</b> {@code categorias} (CategoriaRepository para resolución)</li>
 *   <li><b>Depende de:</b> {@code personas} (PersonaRepository para responsables)</li>
 *   <li><b>Depende de:</b> {@code estructuras} (Estacion para asignación, ResultadoCreacionEstacionDTO)</li>
 *   <li><b>Depende de:</b> {@code ubicaciones} (Ubicacion para resolución)</li>
 *   <li><b>Depende de:</b> {@code ml} (PlantillaLoaderService para detección inteligente)</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>controller</b>: ImportacionController con endpoints REST POST</li>
 *   <li><b>dto</b>: ~59 DTOs en subpaquetes (formato/matriz, formato/clavevalor, agrupacion, interno)</li>
 *   <li><b>service</b>: IImportacionService principal</li>
 *   <li><b>service/analisis</b>: 9 interfaces + implementaciones para análisis de archivos</li>
 *   <li><b>service/analisis/lectores</b>: Lectores de formatos específicos</li>
 *   <li><b>service/persistencia</b>: 6 interfaces + implementaciones para resolución y persistencia</li>
 *   <li><b>service/cache</b>: Gestión de sesiones de importación temporal</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.importacion.service.IImportacionService
 * @see com.upeu.gestioninventario.importacion.dto.formato.respuesta.RespuestaImportacionDTO
 * @see com.upeu.gestioninventario.importacion.dto.ImportacionResultadoDTO
 */
package com.upeu.gestioninventario.importacion;
