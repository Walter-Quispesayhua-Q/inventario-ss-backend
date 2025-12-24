/**
 * <h1>Módulo: Dashboard</h1>
 *
 * <h2>Propósito</h2>
 * Proporciona métricas y resúmenes estadísticos del sistema de inventario para
 * visualización en panel de control. Agrega datos de múltiples dominios para
 * ofrecer una vista consolidada del estado del sistema.
 *
 * <h2>Qué expone</h2>
 * <p><b>GraphQL Queries (públicas, sin autenticación requerida):</b></p>
 * <ul>
 *   <li>{@code dashboard} - Resumen completo con todos los indicadores</li>
 *   <li>{@code dashboardConteos} - Conteos generales (bienes, categorías, estaciones, usuarios)</li>
 *   <li>{@code dashboardBienesPorCategoria} - Distribución de bienes por categoría</li>
 *   <li>{@code dashboardEstructura} - Resumen de infraestructura física</li>
 *   <li>{@code dashboardEstacionesPorAmbiente} - Estaciones agrupadas por ambiente/edificio</li>
 *   <li>{@code dashboardAlertas} - Alertas del sistema (personas sin usuario, usuarios inactivos)</li>
 * </ul>
 *
 * <h2>Conceptos clave</h2>
 * <ul>
 *   <li><b>DashboardResumenDTO</b>: Agregador principal con todos los indicadores</li>
 *   <li><b>ConteoGeneralDTO</b>: Totales de bienes, categorías, estaciones, personas, usuarios</li>
 *   <li><b>BienesPorCategoriaDTO</b>: Distribución para gráficos tipo pie/donut</li>
 *   <li><b>EstructuraResumenDTO</b>: Edificios, pisos, ambientes, estaciones (con/sin bienes)</li>
 *   <li><b>EstacionesPorAmbienteDTO</b>: Desglose jerárquico edificio→piso→ambiente→estaciones</li>
 *   <li><b>AlertasDTO</b>: Indicadores de atención (personas sin usuario, usuarios inactivos)</li>
 * </ul>
 *
 * <h2>Reglas e invariantes</h2>
 * <ul>
 *   <li>Todas las queries son de solo lectura</li>
 *   <li>Los endpoints son públicos (accesibles sin autenticación)</li>
 *   <li>Datos obtenidos en tiempo real desde repositorios de otros dominios</li>
 *   <li>Incluye los 10 últimos bienes creados en el resumen completo</li>
 * </ul>
 *
 * <h2>Dependencias</h2>
 * <ul>
 *   <li><b>Depende de:</b> {@code inventario} (BienRepository)</li>
 *   <li><b>Depende de:</b> {@code categorias} (CategoriaRepository)</li>
 *   <li><b>Depende de:</b> {@code estructuras} (EstacionRepository, EdificioRepository, PisoRepository, AmbienteRepository)</li>
 *   <li><b>Depende de:</b> {@code auth} (UsuarioRepository)</li>
 *   <li><b>Depende de:</b> {@code personas} (PersonaRepository, BienResumenDTO)</li>
 *   <li><b>No debe depender de:</b> Ningún dominio adicional</li>
 * </ul>
 *
 * <h2>Estructura del paquete</h2>
 * <ul>
 *   <li><b>controller</b>: Controlador GraphQL con queries de dashboard</li>
 *   <li><b>dto</b>: Records para métricas (ConteoGeneral, BienesPorCategoria, Estructura, Alertas)</li>
 *   <li><b>service</b>: IDashboardService y DashboardServiceImpl para agregación de datos</li>
 * </ul>
 *
 * @see com.upeu.gestioninventario.dashboard.dto.DashboardResumenDTO
 * @see com.upeu.gestioninventario.dashboard.service.IDashboardService
 */
package com.upeu.gestioninventario.dashboard;
