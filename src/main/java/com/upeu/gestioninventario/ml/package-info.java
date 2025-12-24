/**
 * <h1>Módulo: ML (Machine Learning / Plantillas Inteligentes)</h1>
 *
 * <h2>Propósito</h2>
 * Sistema de plantillas inteligentes para categorización automática de bienes, extracción
 * de atributos y generación de formularios dinámicos. Carga configuraciones JSON de
 * plantillas al iniciar la aplicación y las persiste en base de datos con campos JSONB
 * para scoring, reconocimiento, patrones y reglas de extracción.
 *
 * <h2>Qué expone</h2>
 * <p><b>Servicios internos (no expone API directa):</b></p>
 * <ul>
 *   <li>{@code PlantillaLoaderService.getPlantillasCache()} - Acceso a plantillas cargadas en memoria</li>
 *   <li>{@code PlantillaLoaderService.getPlantillasCambiadas()} - Plantillas con cambios detectados</li>
 *   <li>Mapeo bidireccional entre ID plantilla y nombre de categoría en BD</li>
 * </ul>
 *
 * <p><b>Consumido por:</b></p>
 * <ul>
 *   <li>{@code categorias} - Sugerencias de plantillas, aplicación a categorías</li>
 *   <li>{@code importacion} - Detección de categorías y extracción de atributos</li>
 *   <li>{@code shared.DataInitializer} - Sincronización de plantillas a BD</li>
 * </ul>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>PlantillaCategoria</b>: Entidad persistida con campos JSONB (scoring, reconocimiento, patrones, inteligencia, aprendizaje)</li>
 *   <li><b>PlantillaAtributo</b>: Atributo de extracción vinculado a TipoAtributo con reglas JSON</li>
 *   <li><b>PlantillaSeedDTO</b>: Deserialización de archivos JSON de plantillas</li>
 *   <li><b>PlantillaLoaderService</b>: Servicio que carga plantillas al inicio con auto-descubrimiento</li>
 *   <li><b>Auto-descubrimiento</b>: Escaneo de carpetas configuradas para cargar plantillas</li>
 *   <li><b>Detección de cambios</b>: Hash SHA-256 para detectar modificaciones en archivos</li>
 * </ul>
 *
 * <h2>Estructura de plantilla JSON</h2>
 * <ul>
 *   <li><b>metadatos</b>: idPlantilla, nombre, version, descripcion</li>
 *   <li><b>scoring</b>: Pesos para clasificación automática</li>
 *   <li><b>reconocimiento</b>: Palabras clave para detectar categoría</li>
 *   <li><b>patronesRegex</b>: Expresiones regulares para extracción</li>
 *   <li><b>atributosExtraccion</b>: Definiciones de campos a extraer con etiqueta, tipo, orden</li>
 *   <li><b>inteligencia</b>: Configuración de reglas inteligentes</li>
 *   <li><b>aprendizaje</b>: Datos para mejora continua</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>idPlantillaSeed es único por plantilla</li>
 *   <li>Plantillas se cargan en @PostConstruct del servicio</li>
 *   <li>Cambios detectados por hash están disponibles para sincronización</li>
 *   <li>Configuración de carpetas y convenciones en archivo plantillas-config.json</li>
 *   <li>Los campos JSONB permiten extensibilidad sin cambios de esquema</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code configuracion} (ISeedStateManager para detección de cambios)</li>
 *   <li><b>Depende de:</b> {@code shared} (PlantillasProperties, IHashCalculator)</li>
 *   <li><b>Depende de:</b> {@code categorias} (TipoAtributo vinculado a PlantillaAtributo)</li>
 *   <li><b>Depende de:</b> {@code auth} (Usuario para auditoría de PlantillaCategoria)</li>
 *   <li><b>Es usado por:</b> {@code categorias}, {@code importacion}, {@code shared.DataInitializer}</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>dto.seed</b>: Records para deserialización de JSON (PlantillaSeedDTO, MetadatosSeedDTO, AtributoExtraccionSeedDTO)</li>
 *   <li><b>dto.plantilla</b>: DTOs adicionales como CampoMetadata</li>
 *   <li><b>mapper</b>: MapStruct mappers (PlantillaCategoriaMapper, PlantillaAtributoMapper)</li>
 *   <li><b>model</b>: Entidades JPA (PlantillaCategoria, PlantillaAtributo) con campos JSONB</li>
 *   <li><b>repository</b>: Repositorios JPA para plantillas</li>
 *   <li><b>service</b>: PlantillaLoaderService para carga y cacheo</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.ml.service.PlantillaLoaderService
 * @see com.upeu.gestioninventario.ml.model.PlantillaCategoria
 * @see com.upeu.gestioninventario.ml.model.PlantillaAtributo
 * @see com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO
 */
package com.upeu.gestioninventario.ml;
