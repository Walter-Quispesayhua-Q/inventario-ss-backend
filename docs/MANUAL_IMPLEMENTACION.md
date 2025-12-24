# 🔧 Manual de Implementación
## Sistema de Gestión de Inventario de Bienes

**Versión:** 1.0  
**Última actualización:** Diciembre 2024

---

## 📋 Tabla de Contenidos

1. [Requisitos del Sistema](#1-requisitos-del-sistema)
2. [Configuración del Entorno](#2-configuración-del-entorno)
3. [Instalación Local](#3-instalación-local)
4. [Configuración de Base de Datos](#4-configuración-de-base-de-datos)
5. [Variables de Entorno](#5-variables-de-entorno)
6. [Configuración de Estructuras (Crítico)](#6-configuración-de-estructuras-crítico)
7. [Plantillas ML](#7-plantillas-ml)
8. [Despliegue con Docker](#8-despliegue-con-docker)
9. [Migraciones de Base de Datos](#9-migraciones-de-base-de-datos)
10. [Monitoreo y Logs](#10-monitoreo-y-logs)
11. [Troubleshooting](#11-troubleshooting)

---

## 1. Requisitos del Sistema

### Hardware Mínimo

| Componente | Mínimo | Recomendado |
|------------|--------|-------------|
| CPU | 2 cores | 4 cores |
| RAM | 4 GB | 8 GB |
| Disco | 20 GB | 50 GB SSD |

### Software Requerido

| Software | Versión | Uso |
|----------|---------|-----|
| **Java** | 21 LTS | Runtime de la aplicación |
| **PostgreSQL** | 17+ | Base de datos |
| **Docker Desktop** | Última | Despliegue containerizado |
| **Maven** | 3.9+ | Build (incluido via wrapper) |

### Verificar Java

```powershell
java -version
# Debe mostrar: openjdk version "21.x.x"
```

### Verificar Docker

```powershell
docker --version
docker compose version
```

---

## 2. Configuración del Entorno

### 2.1 Clonar el Repositorio

```powershell
git clone <URL_DEL_REPOSITORIO>
cd Inventario
```

### 2.2 Configurar JAVA_HOME (Windows)

```powershell
# Temporal (sesión actual)
$env:JAVA_HOME = "C:\Users\<TU_USUARIO>\.jdks\ms-21.0.9"

# Verificar
echo $env:JAVA_HOME
```

### 2.3 Estructura del Proyecto

```
src/main/
├── java/com/upeu/gestioninventario/
│   ├── auth/           # Autenticación y seguridad
│   ├── categorias/     # Gestión de categorías
│   ├── configuracion/  # Configuración del sistema
│   ├── dashboard/      # Panel de control
│   ├── estructuras/    # Edificios, pisos, ambientes, estaciones
│   ├── importacion/    # Importación desde Excel
│   ├── inventario/     # Gestión de bienes
│   ├── ml/             # Machine Learning y plantillas
│   ├── personas/       # Gestión de personas
│   ├── shared/         # Componentes compartidos
│   └── ubicaciones/    # Departamentos y ubicaciones
└── resources/
    ├── db/migration/   # Migraciones Flyway
    ├── graphql/        # Schemas GraphQL
    └── seed/           # Datos de inicialización
        ├── estructuras/   # ⚠️ Configuración de edificios
        └── plantillas/    # Plantillas ML por categoría
```

---

## 3. Instalación Local

### 3.1 Con IntelliJ IDEA

1. Abrir el proyecto en IntelliJ IDEA
2. Verificar que el SDK sea Java 21
3. Ejecutar PostgreSQL localmente (puerto 5432)
4. Ejecutar `InventarioApplication.java`

### 3.2 Con Maven (Terminal)

```powershell
# Configurar Java
$env:JAVA_HOME = "C:\Users\<TU_USUARIO>\.jdks\ms-21.0.9"

# Compilar sin tests
.\mvnw.cmd clean compile

# Ejecutar la aplicación
.\mvnw.cmd spring-boot:run
```

### 3.3 Endpoints Disponibles

| Servicio | URL |
|----------|-----|
| API REST | http://localhost:8080 |
| GraphQL Playground | http://localhost:8080/graphiql |
| Health Check | http://localhost:8080/actuator/health |

---

## 4. Configuración de Base de Datos

### 4.1 Crear Base de Datos (PostgreSQL)

```sql
CREATE DATABASE gestion_inventario_db;
CREATE USER inventario_user WITH PASSWORD 'tu_password_seguro';
GRANT ALL PRIVILEGES ON DATABASE gestion_inventario_db TO inventario_user;
```

### 4.2 Schemas Requeridos

El sistema usa múltiples schemas:

| Schema | Contenido |
|--------|-----------|
| `inventario` | Bienes, categorías |
| `estructuras` | Edificios, pisos, ambientes |
| `auth` | Usuarios, roles, permisos |
| `auditoria` | Logs del sistema |

### 4.3 Archivo de Configuración

`src/main/resources/application.properties`:

```properties
# Base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/gestion_inventario_db
spring.datasource.username=inventario_user
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway (desarrollo: deshabilitado, producción: habilitado)
spring.flyway.enabled=false
```

---

## 5. Variables de Entorno

### 5.1 Archivo .env

Crear archivo `.env` en la raíz del proyecto:

```properties
# Base de Datos
DB_PASSWORD=password_seguro_aqui

# JWT
JWT_SECRET=clave_secreta_muy_larga_minimo_256_bits

# Email (SMTP)
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=password_de_aplicacion_gmail
```

### 5.2 Variables Requeridas

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DB_PASSWORD` | Contraseña PostgreSQL | `MiP@ssw0rd!` |
| `JWT_SECRET` | Clave para tokens JWT | `abc123...` (256+ bits) |
| `MAIL_USERNAME` | Email para SMTP | `sistema@empresa.com` |
| `MAIL_PASSWORD` | App Password de Gmail | `xxxx xxxx xxxx xxxx` |

---

## 6. Configuración de Estructuras (Crítico)

> ⚠️ **MUY IMPORTANTE**: Configurar ANTES de usar el sistema.

### 6.1 Ubicación de Archivos

```
src/main/resources/seed/estructuras/
```

### 6.2 Estructura del JSON

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
            }
          ]
        }
      ]
    }
  ]
}
```

### 6.3 Tipos de Entorno Disponibles

| Entorno | Código |
|---------|--------|
| Laboratorio | `LABORATORIO` |
| Oficina | `OFICINA` |
| Almacén | `ALMACEN` |
| Aula | `AULA` |
| Sala de Reuniones | `SALA_REUNIONES` |

### 6.4 Regla de Oro

> 💡 **MAPEAR SOLO LO NECESARIO**
> 
> No es necesario mapear todo el edificio. Solo configura las áreas que realmente vas a utilizar para el inventario.

---

## 7. Plantillas ML

### 7.1 Ubicación

```
src/main/resources/seed/plantillas/
├── computacion/      # CPUs, laptops, servidores
├── perifericos/      # Monitores, impresoras
├── redes/            # Routers, switches
├── audiovisual/      # Proyectores, televisores
├── climatizacion/    # Aires, ventiladores
├── energia/          # UPS, transformadores
└── ...               # Más categorías
```

### 7.2 Estructura de una Plantilla

```json
{
  "metadatos": {
    "idPlantilla": "TPL-XXX-2025-V1",
    "nombre": "NOMBRE_CATEGORIA",
    "version": "1.0"
  },
  "reconocimiento": {
    "palabrasClave": { ... },
    "palabrasExclusion": { ... }
  },
  "scoring": { ... },
  "atributosExtraccion": [ ... ]
}
```

### 7.3 Exclusiones Mutuas

Para evitar clasificaciones incorrectas, las plantillas incluyen exclusiones:

```json
"palabrasExclusion": {
  "absoluta": ["termoventilador", "convector", "calefactor"],
  "fuerte": ["temperatura", "calefaccion"],
  "media": [...],
  "debil": [...]
}
```

---

## 8. Despliegue con Docker

### 8.1 Construir Imagen

```powershell
# Configurar Java
$env:JAVA_HOME = "C:\Users\<TU_USUARIO>\.jdks\ms-21.0.9"

# Construir imagen Docker
.\mvnw.cmd -DskipTests spring-boot:build-image "-Dspring-boot.build-image.imageName=inventario:local"
```

### 8.2 Verificar Imagen

```powershell
docker images | findstr inventario
```

### 8.3 Docker Compose

Archivo `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: gestion_inventario_db
      POSTGRES_USER: inventario_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    image: inventario:local
    depends_on:
      - postgres
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/gestion_inventario_db
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

### 8.4 Comandos Docker

```powershell
# Levantar servicios
docker compose up -d

# Ver estado
docker compose ps

# Ver logs
docker compose logs -f

# Detener
docker compose down

# Reset completo (elimina datos)
docker compose down -v
```

---

## 9. Migraciones de Base de Datos

### 9.1 Ubicación de Migraciones

```
src/main/resources/db/migration/
├── V1__create_schemas.sql
├── V2__create_auth_tables.sql
├── V3__create_ubicaciones.sql
├── V4__create_estructuras.sql
├── V5__create_personas.sql
├── V6__create_categorias.sql
├── V7__create_inventario.sql
└── V8__create_indices.sql
```

### 9.2 Perfiles de Flyway

| Perfil | Flyway | Uso |
|--------|--------|-----|
| `dev` | Deshabilitado | Desarrollo (usa JPA) |
| `prod` | Habilitado | Producción (ejecuta migraciones) |

### 9.3 Ejecutar Migraciones Manualmente

```powershell
# Forzar migraciones en desarrollo
.\mvnw.cmd flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/gestion_inventario_db
```

---

## 10. Monitoreo y Logs

### 10.1 Logs de Aplicación

```powershell
# Docker
docker compose logs backend -f

# Local
# Los logs se muestran en consola
```

### 10.2 Niveles de Log

| Nivel | Uso |
|-------|-----|
| `ERROR` | Errores críticos |
| `WARN` | Advertencias |
| `INFO` | Información general |
| `DEBUG` | Depuración (solo desarrollo) |

### 10.3 Configurar Nivel de Log

En `application.properties`:

```properties
# Producción
logging.level.root=INFO

# Desarrollo
logging.level.com.upeu.gestioninventario=DEBUG
```

---

## 11. Troubleshooting

### Error: JAVA_HOME no definido

```powershell
$env:JAVA_HOME = "C:\Users\<TU_USUARIO>\.jdks\ms-21.0.9"
```

### Error: Docker daemon not running

Abrir Docker Desktop y esperar a que inicie completamente.

### Error: Puerto 5432 en uso

```powershell
# Verificar qué usa el puerto
netstat -ano | findstr 5432

# Cambiar puerto en docker-compose.yml
ports:
  - "5433:5432"
```

### Error: Tablas no existen

1. Verificar perfil activo: `SPRING_PROFILES_ACTIVE=prod`
2. Verificar Flyway habilitado en `application-prod.properties`
3. Ejecutar migraciones manualmente

### Error: "Ambiente no encontrado" en importación

1. Verificar que el ambiente existe en el JSON de estructuras
2. Reiniciar la aplicación después de modificar el JSON
3. Verificar en base de datos: `SELECT * FROM estructuras.ambiente;`

### Error: Clasificación incorrecta de bienes

1. Revisar las exclusiones en la plantilla correspondiente
2. Agregar términos de exclusión si es necesario
3. Reiniciar la aplicación

---

## Comandos Útiles

### Maven

```powershell
# Compilar
.\mvnw.cmd clean compile

# Tests
.\mvnw.cmd test

# Empaquetar
.\mvnw.cmd package -DskipTests

# Ejecutar
.\mvnw.cmd spring-boot:run
```

### Docker

```powershell
# Construir imagen
.\mvnw.cmd spring-boot:build-image "-Dspring-boot.build-image.imageName=inventario:local"

# Levantar
docker compose up -d

# Logs
docker compose logs -f

# Reiniciar backend
docker compose restart backend

# Limpiar todo
docker compose down -v
docker system prune -a
```

---

*© 2024 Sistema de Gestión de Inventario - UPeU*
