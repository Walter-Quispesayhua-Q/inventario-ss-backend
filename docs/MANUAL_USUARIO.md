# 📘 Manual de Usuario
## Sistema de Gestión de Inventario de Bienes

**Versión:** 1.0  
**Última actualización:** Diciembre 2024

---

## 📋 Tabla de Contenidos

1. [Introducción](#1-introducción)
2. [Acceso al Sistema](#2-acceso-al-sistema)
3. [Dashboard](#3-dashboard)
4. [Gestión de Estructuras](#4-gestión-de-estructuras)
5. [Gestión de Inventario](#5-gestión-de-inventario)
6. [Importación de Bienes](#6-importación-de-bienes)
7. [Gestión de Categorías](#7-gestión-de-categorías)
8. [Gestión de Personas](#8-gestión-de-personas)
9. [Gestión de Usuarios](#9-gestión-de-usuarios)
10. [Preguntas Frecuentes](#10-preguntas-frecuentes)

---

## 1. Introducción

El **Sistema de Gestión de Inventario de Bienes** permite administrar de forma integral los activos de la institución, incluyendo:

- ✅ Registro y seguimiento de bienes
- ✅ Organización por ubicación física (edificios, pisos, ambientes, estaciones)
- ✅ Importación masiva desde archivos Excel
- ✅ Clasificación inteligente por categorías
- ✅ Reportes y estadísticas en tiempo real

### Roles de Usuario

| Rol | Permisos |
|-----|----------|
| **ADMIN** | Acceso completo al sistema |
| **USER_INTERNO** | Gestión de inventario y estructuras |
| **USER_OBSERVADOR** | Solo lectura |

---

## 2. Acceso al Sistema

### 2.1 Credenciales por Defecto

> ⚠️ **IMPORTANTE**: Cambiar estas credenciales en producción.

#### Usuario Administrador

| Campo | Valor |
|-------|-------|
| **Email** | `wali@inventario.com` |
| **Contraseña** | `admin123` |
| **Rol** | ADMIN |

#### Usuario Gestor

| Campo | Valor |
|-------|-------|
| **Email** | `gestor@inventario.com` |
| **Contraseña** | `gestor123` |
| **Rol** | USER_INTERNO |

### 2.2 Iniciar Sesión

1. Abrir el navegador web
2. Ingresar a la URL del sistema
3. Introducir email y contraseña
4. Hacer clic en **"Iniciar Sesión"**

### 2.3 Cerrar Sesión

1. Hacer clic en el ícono de usuario (esquina superior derecha)
2. Seleccionar **"Cerrar Sesión"**

---

## 3. Dashboard

El Dashboard muestra un resumen del estado del inventario:

### 3.1 Estadísticas Principales

- **Total de Bienes**: Cantidad total de activos registrados
- **Bienes por Categoría**: Distribución gráfica
- **Usuarios Activos**: Cantidad de usuarios en el sistema
- **Infraestructura**: Estado de edificios y estaciones

### 3.2 Indicadores

| Indicador | Descripción |
|-----------|-------------|
| 🟢 Verde | Operativo |
| 🟡 Amarillo | Requiere atención |
| 🔴 Rojo | Crítico |

---

## 4. Gestión de Estructuras

> ⚠️ **CRÍTICO**: Configurar las estructuras **ANTES** de registrar bienes.

### 4.1 Jerarquía de Estructuras

```
📍 Ubicación
└── 🏢 Edificio
    └── 🏛️ Piso
        └── 🚪 Ambiente
            └── 💻 Estación
```

### 4.2 Crear Edificio

1. Ir a **Estructuras** → **Edificios**
2. Clic en **"Nuevo Edificio"**
3. Completar:
   - Nombre del edificio
   - Código (ej: `EP-IS`)
   - Ubicación
4. Guardar

### 4.3 Crear Piso

1. Ir a **Estructuras** → **Pisos**
2. Clic en **"Nuevo Piso"**
3. Completar:
   - Número de piso
   - Nombre descriptivo
   - Seleccionar edificio
4. Guardar

### 4.4 Crear Ambiente

1. Ir a **Estructuras** → **Ambientes**
2. Clic en **"Nuevo Ambiente"**
3. Completar:
   - Nombre (ej: "Laboratorio A")
   - Código (ej: `LAB-A`)
   - Tipo de entorno (Laboratorio, Oficina, etc.)
   - Seleccionar piso
4. Guardar

### 4.5 Crear Estación

1. Ir a **Estructuras** → **Estaciones**
2. Clic en **"Nueva Estación"**
3. Completar:
   - Código de estación (ej: `PC-01`)
   - Descripción
   - Seleccionar ambiente
4. Guardar

---

## 5. Gestión de Inventario

### 5.1 Ver Bienes

1. Ir a **Inventario** → **Bienes**
2. Usar filtros para buscar:
   - Por categoría
   - Por ubicación
   - Por código CAF
   - Por estado

### 5.2 Registrar Nuevo Bien

1. Ir a **Inventario** → **Bienes**
2. Clic en **"Nuevo Bien"**
3. Completar formulario:
   - Código CAF
   - Descripción
   - Categoría
   - Ubicación (Estación)
   - Marca, modelo, serie
4. Guardar

### 5.3 Editar Bien

1. Buscar el bien en la lista
2. Clic en el ícono de **editar** ✏️
3. Modificar los campos necesarios
4. Guardar cambios

### 5.4 Estados de Bienes

| Estado | Descripción |
|--------|-------------|
| **ACTIVO** | Bien operativo en uso |
| **BAJA** | Bien dado de baja |
| **REPARACION** | En proceso de reparación |
| **PRESTAMO** | Prestado temporalmente |

---

## 6. Importación de Bienes

### 6.1 Preparar Archivo Excel

El archivo debe contener:

| Columna | Requerido | Ejemplo |
|---------|-----------|---------|
| Código CAF | ✅ | `CAF-2024-001` |
| Descripción | ✅ | `Monitor LED 24"` |
| Marca | ⬜ | `Samsung` |
| Modelo | ⬜ | `S24B300` |
| Serie | ⬜ | `ABC123456` |

### 6.2 Proceso de Importación

1. Ir a **Inventario** → **Importar**
2. Seleccionar el **ambiente destino**
3. Clic en **"Seleccionar archivo"**
4. Elegir el archivo Excel
5. El sistema mostrará una **vista previa**
6. Verificar los datos detectados
7. Clic en **"Importar"**

### 6.3 Resultado de Importación

Después de la importación, se mostrará:

- ✅ **Bienes importados correctamente**
- ⚠️ **Bienes con advertencias**
- ❌ **Bienes con errores** (con detalle del error)

---

## 7. Gestión de Categorías

### 7.1 Ver Categorías

1. Ir a **Categorías**
2. Se muestra lista con:
   - Nombre de categoría
   - Descripción
   - Estado (Activo/Inactivo)

### 7.2 Categorías Inteligentes (ML)

El sistema incluye categorías con **detección automática** basada en Machine Learning:

- Computación (laptops, CPUs, monitores)
- Periféricos (impresoras, teclados)
- Redes (routers, switches)
- Audiovisual (proyectores, televisores)
- Climatización (aires, ventiladores)
- Y más...

### 7.3 Crear Categoría

1. Ir a **Categorías**
2. Clic en **"Nueva Categoría"**
3. Completar:
   - Nombre
   - Descripción
4. Guardar

---

## 8. Gestión de Personas

### 8.1 Ver Personas

1. Ir a **Personas**
2. Se muestra lista del personal registrado

### 8.2 Registrar Persona

1. Clic en **"Nueva Persona"**
2. Completar:
   - Nombres
   - Apellidos
   - Email
   - Código (opcional)
3. Guardar

---

## 9. Gestión de Usuarios

> 🔒 **Solo disponible para ADMIN**

### 9.1 Crear Usuario desde Persona

1. Ir a **Usuarios**
2. Clic en **"Nuevo Usuario"**
3. Seleccionar persona existente
4. Asignar:
   - Email
   - Contraseña temporal
   - Rol
   - Departamento
5. Guardar

### 9.2 Activar/Desactivar Usuario

1. Buscar usuario en la lista
2. Clic en el switch de **estado**
3. Confirmar acción

### 9.3 Cambiar Rol de Usuario

1. Editar usuario
2. Seleccionar nuevo rol
3. Guardar cambios

---

## 10. Preguntas Frecuentes

### ¿Cómo recupero mi contraseña?
Contactar al administrador del sistema para restablecer la contraseña.

### ¿Por qué no puedo importar bienes?
Verificar que:
1. El ambiente destino existe y está configurado
2. Las estaciones están creadas
3. El archivo Excel tiene el formato correcto

### ¿Por qué un bien se clasificó incorrectamente?
El sistema usa Machine Learning para detectar categorías. Si hay errores:
1. Editar el bien manualmente
2. Reportar al administrador para mejorar las plantillas

### ¿Cómo exporto el inventario?
1. Ir a **Inventario** → **Bienes**
2. Aplicar filtros si es necesario
3. Clic en **"Exportar"**
4. Seleccionar formato (Excel, PDF)

### ¿Puedo ver el historial de cambios?
Sí, ir a **Auditoría** para ver el registro de todas las acciones realizadas en el sistema.

---

## Soporte

Para soporte técnico, contactar al equipo de TI o consultar el **Manual de Implementación**.

---

*© 2024 Sistema de Gestión de Inventario - UPeU*
