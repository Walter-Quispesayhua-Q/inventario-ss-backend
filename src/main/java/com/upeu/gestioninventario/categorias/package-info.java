/**
 * <h1>Módulo: Categorías</h1>
 *
 * <h2>Propósito</h2>
 * Sistema de clasificación de bienes mediante categorías dinámicas con soporte para
 * atributos personalizables y plantillas inteligentes. Permite organizar el inventario
 * y definir formularios dinámicos basados en plantillas predefinidas.
 *
 * <h2>Qué expone</h2>
 * <p><b>GraphQL Mutations:</b></p>
 * <ul>
 *   <li>{@code crearCategoria} - Crear nueva categoría (ADMIN, USER_INTERNO)</li>
 *   <li>{@code actualizarCategoria} - Actualizar categoría existente (ADMIN, USER_INTERNO)</li>
 *   <li>{@code eliminarCategoria} - Eliminar categoría sin bienes asociados (ADMIN, USER_INTERNO)</li>
 *   <li>{@code aplicarPlantillaACategoria} - Aplicar plantilla inteligente a categoría (ADMIN, USER_INTERNO)</li>
 * </ul>
 *
 * <p><b>GraphQL Queries:</b></p>
 * <ul>
 *   <li>{@code categorias} - Consulta paginada con filtros (estado, búsqueda)</li>
 *   <li>{@code categoriasSimplesDisponibles} - Lista simplificada para dropdowns</li>
 *   <li>{@code sugerirPlantillasParaCategoria} - Sugerir plantillas por nombre de categoría</li>
 *   <li>{@code atributosParaFormularioPorCategoria} - Obtener campos de formulario dinámico</li>
 * </ul>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>Categoria</b>: Entidad principal con nombre, descripción, icono, color y estado</li>
 *   <li><b>TipoAtributo</b>: Definición de tipo de dato (TEXTO, NUMERO, FECHA, LISTA, etc.)</li>
 *   <li><b>CategoriaAtributo</b>: Relación many-to-many entre categoría y tipo de atributo con etiqueta y requerido</li>
 *   <li><b>esInteligente</b>: Flag que indica si la categoría tiene plantilla ML asociada</li>
 *   <li><b>visible</b>: Control de visibilidad para categorías "borradores"</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>Nombre de categoría es único en el sistema</li>
 *   <li>No se puede eliminar categoría con bienes asociados</li>
 *   <li>Soporta soft delete mediante {@code fechaEliminacion}</li>
 *   <li>Categorías eliminadas pueden reactivarse al crear con mismo nombre</li>
 *   <li>Categorías recién creadas son visibles por defecto</li>
 *   <li>Se marcan como "inteligentes" si existe plantilla ML coincidente</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code shared} (Auditable, OperacionResultadoDTO, PaginacionUtils, GenericSpecifications)</li>
 *   <li><b>Depende de:</b> {@code auth} (Usuario para auditoría)</li>
 *   <li><b>Depende de:</b> {@code ml} (PlantillaCategoria, PlantillaAtributo, PlantillaLoaderService)</li>
 *   <li><b>Depende de:</b> {@code inventario} (BienRepository para validar eliminación, AtributoFormularioDTO)</li>
 *   <li><b>No debe depender de:</b> otros dominios</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>controller</b>: Controlador GraphQL para operaciones CRUD y consultas</li>
 *   <li><b>dto</b>: Records para creación, actualización, respuestas y paginación</li>
 *   <li><b>dto.tipoatributo</b>: DTOs específicos para tipos de atributo</li>
 *   <li><b>mapper</b>: MapStruct mappers (CategoriaMapper, CategoriaAtributoMapper, TipoAtributoMapper)</li>
 *   <li><b>model</b>: Entidades JPA (Categoria, CategoriaAtributo, TipoAtributo)</li>
 *   <li><b>repository</b>: Repositorios JPA con queries optimizados</li>
 *   <li><b>repository.specifications</b>: Specifications JPA para filtros dinámicos</li>
 *   <li><b>service</b>: ICategoriaService, IFormularioDinamicoService y sus implementaciones</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.categorias.model.Categoria
 * @see com.upeu.gestioninventario.categorias.service.ICategoriaService
 * @see com.upeu.gestioninventario.categorias.service.IFormularioDinamicoService
 */
package com.upeu.gestioninventario.categorias;
