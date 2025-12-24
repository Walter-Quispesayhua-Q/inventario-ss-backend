/**
 * <h1>Módulo: Configuración</h1>
 *
 * <h2>Propósito</h2>
 * Gestión de parámetros del sistema y control de estado de archivos de semilla (seed).
 * Proporciona infraestructura para almacenar configuraciones clave-valor y detectar
 * cambios en archivos de inicialización para recarga inteligente de datos.
 *
 * <h2>Qué expone</h2>
 * <p><b>Servicios internos (no expuestos vía API):</b></p>
 * <ul>
 *   <li>{@code ISeedStateManager.necesitaRecarga} - Verificar si archivo seed cambió por hash SHA-256</li>
 *   <li>{@code ISeedStateManager.registrarCarga} - Registrar carga exitosa de archivo seed</li>
 * </ul>
 *
 * <p><b>Nota:</b> Este módulo actualmente no expone endpoints GraphQL. Sus DTOs y mapper
 * están preparados para futura API de gestión de parámetros del sistema.</p>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>ParametroSistema</b>: Almacena configuraciones clave-valor con descripción</li>
 *   <li><b>SeedFileState</b>: Registro del estado de archivos de inicialización con hash SHA-256</li>
 *   <li><b>Detección de cambios</b>: Compara hash de contenido para decidir si recargar datos</li>
 *   <li><b>Contador de recargas</b>: Tracking de cuántas veces se ha recargado cada archivo</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>Clave de parámetro es única y sirve como identificador (PK)</li>
 *   <li>Nombre de archivo seed es único</li>
 *   <li>Hash vacío o nulo fuerza recarga del archivo</li>
 *   <li>Contador de recargas se incrementa automáticamente en cada @PreUpdate</li>
 *   <li>Los archivos mantienen registro de primera y última carga</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> Ningún otro módulo (es módulo de infraestructura base)</li>
 *   <li><b>Es usado por:</b> {@code ml} (PlantillaLoaderService para detectar cambios en seeds)</li>
 *   <li><b>Es usado por:</b> {@code shared} (DataInitializer para gestión de datos iniciales)</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>dto</b>: Records para CRUD de parámetros (ParametroSistemaDTO, Creación, Actualización)</li>
 *   <li><b>mapper</b>: MapStruct mapper para ParametroSistema</li>
 *   <li><b>model</b>: Entidades JPA (ParametroSistema, SeedFileState)</li>
 *   <li><b>repository</b>: Repositorio JPA para SeedFileState</li>
 *   <li><b>service</b>: ISeedStateManager y su implementación para gestión de estado de seeds</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.configuracion.model.ParametroSistema
 * @see com.upeu.gestioninventario.configuracion.model.SeedFileState
 * @see com.upeu.gestioninventario.configuracion.service.ISeedStateManager
 */
package com.upeu.gestioninventario.configuracion;
