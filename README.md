# Sistema de Gestión de Inventario - Backend

API REST y GraphQL para el Sistema de Gestión de Inventario de Bienes.

## 📚 Documentación

| Manual | Descripción |
|--------|-------------|
| [📘 Manual de Usuario](docs/MANUAL_USUARIO.md) | Guía para usuarios finales del sistema |
| [🔧 Manual de Implementación](docs/MANUAL_IMPLEMENTACION.md) | Instalación, configuración y despliegue |
| [🧪 Manual de Testing](docs/MANUAL_TESTING.md) | Estrategia de testing y casos de prueba |

## Tecnologías

- Java 21 LTS
- Spring Boot 3.5.9
- PostgreSQL 17
- GraphQL
- Docker

## Requisitos Previos

- **Java 21** instalado (verificar con `java -version`)
- **Docker Desktop** instalado y corriendo
- **Maven** (incluido via wrapper)

## Desarrollo Local

### Opción 1: Ejecutar con IntelliJ IDEA

1. Abrir el proyecto en IntelliJ
2. Asegurarse de que el SDK sea Java 21
3. Tener PostgreSQL corriendo en `localhost:5432`
4. Ejecutar `InventarioApplication.java`

### Opción 2: Ejecutar con Maven

```bash
# Windows PowerShell
$env:JAVA_HOME = "C:\Users\<TU_USUARIO>\.jdks\ms-21.0.9"
.\mvnw.cmd spring-boot:run
```

## Despliegue con Docker

### Paso 1: Construir la imagen Docker

```powershell
# Configurar Java
$env:JAVA_HOME = "C:\Users\<TU_USUARIO>\.jdks\ms-21.0.9"

# Ir al directorio del proyecto
cd "C:\ruta\al\proyecto\Inventario"

# Construir la imagen (sin ejecutar tests)
.\mvnw.cmd -DskipTests spring-boot:build-image "-Dspring-boot.build-image.imageName=inventario:local"
```

### Paso 2: Verificar que la imagen existe

```powershell
docker images | findstr inventario
```

### Paso 3: Levantar los servicios

```powershell
docker compose up -d
```

Esto levantara:
- **PostgreSQL 17** en puerto 5432
- **API Backend** en puerto 8080

### Paso 4: Verificar el estado

```powershell
# Ver estado de contenedores
docker compose ps

# Ver logs en tiempo real
docker compose logs -f

# Ver logs solo del backend
docker compose logs backend -f
```

## Acceso a la Aplicacion

| Servicio | URL |
|----------|-----|
| API REST | http://localhost:8080 |
| GraphQL Playground | http://localhost:8080/graphiql |

## Comandos Utiles de Docker

```powershell
# Levantar servicios en segundo plano
docker compose up -d

# Detener servicios
docker compose down

# Reiniciar solo el backend
docker compose restart backend

# Ver logs en tiempo real
docker compose logs -f

# Reset completo (elimina base de datos)
docker compose down -v
```

## Variables de Entorno

El archivo `.env` debe contener:

```properties
DB_PASSWORD=<password_postgres>
JWT_SECRET=<clave_secreta_jwt>
MAIL_USERNAME=<email_para_smtp>
MAIL_PASSWORD=<password_app_gmail>
```

## Perfiles de Spring

| Perfil | Uso | Flyway |
|--------|-----|--------|
| `dev` | Desarrollo local | Deshabilitado |
| `prod` | Docker/Produccion | Habilitado |

## Estructura del Proyecto

```
src/main/
├── java/com/upeu/gestioninventario/
│   ├── auth/           # Autenticacion y seguridad
│   ├── categorias/     # Gestion de categorias
│   ├── configuracion/  # Configuracion del sistema
│   ├── estructuras/    # Edificios, pisos, ambientes
│   ├── inventario/     # Bienes e inventario
│   ├── ml/             # Machine Learning y plantillas
│   ├── personas/       # Gestion de personas
│   ├── shared/         # Componentes compartidos
│   └── ubicaciones/    # Gestion de ubicaciones
└── resources/
    ├── db/migration/   # Migraciones Flyway
    ├── graphql/        # Schemas GraphQL
    └── seed/           # Datos de inicializacion
```

## Credenciales por Defecto

> ⚠️ **IMPORTANTE**: Cambiar estas credenciales en producción.

### Usuario Administrador (ADMIN)

| Campo | Valor |
|-------|-------|
| Email | `wali@inventario.com` |
| Contraseña | `admin123` |
| Código | `ADMIN-001` |
| Rol | `ADMIN` - Acceso completo |

### Usuario Gestor Interno (USER_INTERNO)

| Campo | Valor |
|-------|-------|
| Email | `gestor@inventario.com` |
| Contraseña | `gestor123` |
| Código | `GESTOR-001` |
| Rol | `USER_INTERNO` - Gestión de inventario |

---

## ⚠️ CONFIGURACIÓN CRÍTICA: Módulo de Estructuras

> **MUY IMPORTANTE**: Antes de usar el sistema, es **obligatorio** configurar correctamente el módulo de estructuras (edificios, pisos, ambientes y estaciones).

### ¿Por qué es crítico?

El sistema utiliza plantillas JSON para definir la estructura física del edificio. Si las estructuras no están correctamente mapeadas:
- ❌ No se podrán guardar bienes en ubicaciones específicas
- ❌ Las importaciones de inventario fallarán
- ❌ Los reportes de ubicación estarán incompletos

### 🔧 MÉTODO RECOMENDADO: Actualizar el JSON Directamente

> ⭐ **SE RECOMIENDA** actualizar directamente el archivo JSON de estructuras. Este es el método más confiable y completo.

#### Paso 1: Ubicar el Archivo JSON de Estructuras

El archivo de configuración de estructuras se encuentra en:
```
src/main/resources/seed/estructuras/
```

#### Paso 2: Editar el JSON con la Estructura del Edificio

Abre el archivo JSON y define tu estructura. **Mapea SOLO lo que vas a usar**:

```json
{
  "edificios": [
    {
      "nombre": "EP Ingeniería de Sistemas",
      "codigo": "EP-IS",
      "pisos": [
        {
          "nombre": "Primer Piso",
          "numero": 1,
          "ambientes": [
            {
              "nombre": "Laboratorio A",
              "codigo": "LAB-A",
              "entorno": "LABORATORIO",
              "estaciones": [
                { "codigo": "PC-01", "descripcion": "Estación 1" },
                { "codigo": "PC-02", "descripcion": "Estación 2" },
                { "codigo": "PC-03", "descripcion": "Estación 3" }
              ]
            },
            {
              "nombre": "Laboratorio B",
              "codigo": "LAB-B",
              "entorno": "LABORATORIO",
              "estaciones": [
                { "codigo": "PC-01", "descripcion": "Estación 1" },
                { "codigo": "PC-02", "descripcion": "Estación 2" }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

#### Paso 3: Reiniciar la Aplicación

Después de editar el JSON, reinicia la aplicación para que cargue las nuevas estructuras:

```bash
# Si usas Docker
docker compose restart backend

# Si usas desarrollo local
# Reiniciar desde IntelliJ o Maven
```

#### Paso 4: Verificar en Base de Datos

Las estructuras se crearán automáticamente al iniciar la aplicación.

### 📱 MÉTODO ALTERNATIVO: Interfaz de Usuario

> ⚠️ **Solo usar si no se puede editar el JSON directamente.**

Si no tienes acceso al código fuente, puedes crear estructuras desde la interfaz:

1. Iniciar sesión como Administrador
2. Ir a **Estructuras** → **Edificios**
3. Crear edificio → Agregar pisos → Crear ambientes → Crear estaciones

### 💡 Regla Importante: Mapear SOLO lo Necesario

> **NO es necesario mapear TODO el edificio.** Solo mapea las áreas que realmente vas a utilizar para el inventario.

**Ejemplo de estructura mínima:**
```
EP Ingeniería de Sistemas (Edificio)
├── Piso 1
│   ├── Laboratorio A (Ambiente)
│   │   ├── PC-01, PC-02, PC-03 (Estaciones)
│   └── Laboratorio B (Ambiente)
│       └── PC-01, PC-02 (Estaciones)
└── Piso 2
    └── Oficina Administración (Ambiente)
        └── ADMIN-01 (Estación)
```

### Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| "Ambiente no encontrado" | No existe el ambiente en BD | Agregar el ambiente al JSON y reiniciar |
| "Estación no válida" | Código de estación no existe | Agregar la estación en el JSON |
| "Error al guardar bien" | Estructura incompleta | Completar la jerarquía en el JSON |

---

## Solucion de Problemas

### Error: JAVA_HOME no definido

```powershell
$env:JAVA_HOME = "C:\Users\<TU_USUARIO>\.jdks\ms-21.0.9"
```

### Error: Docker daemon not running

Abrir Docker Desktop y esperar a que inicie completamente.

### Error: Nombre de imagen con mayusculas

Docker requiere nombres en minusculas. Usar:
```powershell
"-Dspring-boot.build-image.imageName=inventario:local"
```

### Error: Tablas no existen

Verificar que Flyway este habilitado en el perfil `prod`:
```properties
spring.flyway.enabled=true
```
