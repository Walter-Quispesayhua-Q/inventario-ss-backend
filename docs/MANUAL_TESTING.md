# 🧪 Manual de Testing
## Sistema de Gestión de Inventario de Bienes

**Versión:** 1.0  
**Última actualización:** Diciembre 2024

---

## 📋 Tabla de Contenidos

1. [Estrategia de Testing](#1-estrategia-de-testing)
2. [Configuración del Entorno](#2-configuración-del-entorno)
3. [Tests Unitarios](#3-tests-unitarios)
4. [Tests de Integración](#4-tests-de-integración)
5. [Tests de API REST](#5-tests-de-api-rest)
6. [GraphQL Playground](#6-graphql-playground)
7. [Validación de Plantillas ML](#7-validación-de-plantillas-ml)
8. [Casos de Prueba por Módulo](#8-casos-de-prueba-por-módulo)
9. [Checklist Pre-Deploy](#9-checklist-pre-deploy)
10. [Reportes de Testing](#10-reportes-de-testing)

---

## 1. Estrategia de Testing

### 1.1 Pirámide de Tests

```
        ╱╲
       ╱  ╲       E2E Tests (5%)
      ╱────╲      
     ╱      ╲     Integration Tests (20%)
    ╱────────╲    
   ╱          ╲   Unit Tests (75%)
  ╱────────────╲  
```

### 1.2 Tecnologías

| Tipo | Tecnología |
|------|------------|
| Unit Tests | JUnit 5 + Mockito |
| Integration | Spring Boot Test |
| API Testing | REST Assured / Postman |
| GraphQL | GraphQL Playground |

### 1.3 Cobertura Objetivo

| Capa | Cobertura Mínima |
|------|------------------|
| Services | 80% |
| Controllers | 70% |
| Repositories | 60% |

---

## 2. Configuración del Entorno

### 2.1 Dependencias de Testing

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 2.2 Ejecutar Tests

```powershell
# Todos los tests
.\mvnw.cmd test

# Tests específicos
.\mvnw.cmd test -Dtest=BienServiceTest

# Con reporte de cobertura
.\mvnw.cmd test jacoco:report
```

### 2.3 Base de Datos de Test

Usar H2 en memoria o PostgreSQL de test:

```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## 3. Tests Unitarios

### 3.1 Estructura de Tests

```
src/test/java/com/upeu/gestioninventario/
├── auth/
│   └── service/
│       └── UsuarioServiceTest.java
├── estructuras/
│   └── service/
│       └── EstacionServiceTest.java
├── inventario/
│   └── service/
│       └── BienServiceTest.java
└── ml/
    └── service/
        └── DetectorCategoriaServiceTest.java
```

### 3.2 Ejemplo de Test Unitario

```java
@ExtendWith(MockitoExtension.class)
class BienServiceTest {

    @Mock
    private BienRepository bienRepository;

    @InjectMocks
    private BienServiceImpl bienService;

    @Test
    @DisplayName("Debe crear un bien correctamente")
    void debeCrearBien() {
        // Given
        BienInput input = new BienInput("CAF-001", "Monitor");
        Bien bienEsperado = Bien.builder()
            .codigoCaf("CAF-001")
            .descripcion("Monitor")
            .build();
        
        when(bienRepository.save(any(Bien.class)))
            .thenReturn(bienEsperado);

        // When
        Bien resultado = bienService.crear(input);

        // Then
        assertNotNull(resultado);
        assertEquals("CAF-001", resultado.getCodigoCaf());
        verify(bienRepository).save(any(Bien.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si CAF duplicado")
    void debeLanzarExcepcionSiCafDuplicado() {
        // Given
        when(bienRepository.existsByCodigoCaf("CAF-001"))
            .thenReturn(true);

        // When & Then
        assertThrows(DuplicadoException.class, 
            () -> bienService.crear(new BienInput("CAF-001", "Test")));
    }
}
```

---

## 4. Tests de Integración

### 4.1 Configuración

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Transactional
class IntegracionTest {
    // Tests...
}
```

### 4.2 Ejemplo de Test de Integración

```java
@SpringBootTest
@AutoConfigureMockMvc
class BienControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BienRepository bienRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void debeObtenerListaDeBienes() throws Exception {
        mockMvc.perform(get("/api/bienes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
```

---

## 5. Tests de API REST

### 5.1 Endpoints Principales

| Módulo | Endpoint Base |
|--------|---------------|
| Auth | `/api/auth` |
| Bienes | `/api/bienes` |
| Categorías | `/api/categorias` |
| Estructuras | `/api/edificios`, `/api/pisos`, `/api/ambientes`, `/api/estaciones` |
| Personas | `/api/personas` |
| Importación | `/api/importacion` |

### 5.2 Casos de Prueba REST

#### Auth - Login

```bash
# Request
POST /api/auth/login
Content-Type: application/json

{
  "email": "wali@inventario.com",
  "password": "admin123"
}

# Response esperado (200 OK)
{
  "token": "eyJhbG...",
  "email": "wali@inventario.com",
  "roles": ["ADMIN"]
}
```

#### Bienes - Listar

```bash
# Request
GET /api/bienes?page=0&size=10
Authorization: Bearer <token>

# Response esperado (200 OK)
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 15
}
```

#### Bienes - Crear

```bash
# Request
POST /api/bienes
Authorization: Bearer <token>
Content-Type: application/json

{
  "codigoCaf": "CAF-2024-001",
  "descripcion": "Monitor LED 24 pulgadas",
  "estacionId": 5
}

# Response esperado (201 Created)
```

### 5.3 Códigos de Estado Esperados

| Código | Significado |
|--------|-------------|
| 200 | OK - Operación exitosa |
| 201 | Created - Recurso creado |
| 400 | Bad Request - Datos inválidos |
| 401 | Unauthorized - No autenticado |
| 403 | Forbidden - Sin permisos |
| 404 | Not Found - No existe |
| 500 | Server Error - Error interno |

---

## 6. GraphQL Playground

### 6.1 Acceso

URL: `http://localhost:8080/graphiql`

### 6.2 Queries de Ejemplo

#### Listar Bienes

```graphql
query {
  bienes(page: 0, size: 10) {
    content {
      id
      codigoCaf
      descripcion
      categoria {
        nombreCategoria
      }
      estacion {
        codigo
        ambiente {
          nombre
        }
      }
    }
    totalElements
  }
}
```

#### Obtener Estructura Completa

```graphql
query {
  edificios {
    nombre
    pisos {
      numero
      nombre
      ambientes {
        nombre
        codigo
        estaciones {
          codigo
          descripcion
        }
      }
    }
  }
}
```

### 6.3 Mutations de Ejemplo

```graphql
mutation {
  crearBien(input: {
    codigoCaf: "CAF-2024-001"
    descripcion: "Laptop HP ProBook"
    estacionId: 5
  }) {
    id
    codigoCaf
  }
}
```

---

## 7. Validación de Plantillas ML

### 7.1 Test de Clasificación

Verificar que las plantillas clasifiquen correctamente:

| Input | Categoría Esperada |
|-------|-------------------|
| "Laptop HP ProBook 450 Core i7" | Laptop |
| "Monitor Samsung 24 LED Full HD" | Monitor |
| "Termoventilador IMACO 1500W" | Termoventilador |
| "Router Cisco ISR4221 WiFi" | Router |
| "Procesador Intel Core i7-12700K" | Procesador |

### 7.2 Test de Exclusiones

Verificar que NO se clasifique incorrectamente:

| Input | NO debe ser |
|-------|-------------|
| "Termoventilador IMACO 1500W" | ❌ Procesador, ❌ CPU |
| "Intel Core i7 12700K" | ❌ Termoventilador |
| "Monitor Samsung curvo" | ❌ Televisor |

### 7.3 Script de Validación

```java
@Test
void validarClasificacionProcesador() {
    String texto = "Procesador Intel Core i7-12700K 3.6GHz 12 núcleos";
    
    CategoriaDetectada resultado = detectorService.detectar(texto);
    
    assertEquals("PROCESADOR", resultado.getCategoria());
    assertTrue(resultado.getConfianza() > 0.8);
}

@Test
void validarExclusionTermoventilador() {
    String texto = "Termoventilador IMACO 1500W calefactor";
    
    CategoriaDetectada resultado = detectorService.detectar(texto);
    
    assertNotEquals("PROCESADOR", resultado.getCategoria());
    assertEquals("TERMOVENTILADOR", resultado.getCategoria());
}
```

---

## 8. Casos de Prueba por Módulo

### 8.1 Módulo Auth

| ID | Caso | Entrada | Resultado Esperado |
|----|------|---------|-------------------|
| AU-01 | Login válido | Email y password correctos | Token JWT |
| AU-02 | Login inválido | Password incorrecto | 401 Unauthorized |
| AU-03 | Usuario inactivo | Usuario deshabilitado | 403 Forbidden |

### 8.2 Módulo Estructuras

| ID | Caso | Entrada | Resultado Esperado |
|----|------|---------|-------------------|
| ES-01 | Crear edificio | Datos válidos | 201 Created |
| ES-02 | Crear piso | Edificio existente | 201 Created |
| ES-03 | Crear ambiente | Piso existente | 201 Created |
| ES-04 | Crear estación | Ambiente existente | 201 Created |
| ES-05 | Código duplicado | Estación con código existente | 400 Bad Request |

### 8.3 Módulo Inventario

| ID | Caso | Entrada | Resultado Esperado |
|----|------|---------|-------------------|
| IN-01 | Crear bien | Datos completos | 201 Created |
| IN-02 | CAF duplicado | Mismo código CAF | 400 Bad Request |
| IN-03 | Sin estación | Estación no existe | 404 Not Found |
| IN-04 | Actualizar bien | Datos válidos | 200 OK |
| IN-05 | Dar de baja | Estado = BAJA | 200 OK |

### 8.4 Módulo Importación

| ID | Caso | Entrada | Resultado Esperado |
|----|------|---------|-------------------|
| IM-01 | Excel válido | Formato correcto | Lista de bienes |
| IM-02 | Excel vacío | Sin datos | 400 Bad Request |
| IM-03 | Ambiente inexistente | ID inválido | 404 Not Found |
| IM-04 | Clasificación ML | Descripciones | Categorías detectadas |

---

## 9. Checklist Pre-Deploy

### 9.1 Verificaciones Obligatorias

- [ ] Todos los tests unitarios pasan
- [ ] Tests de integración exitosos
- [ ] Cobertura de código ≥ 70%
- [ ] Sin errores de compilación
- [ ] Variables de entorno configuradas
- [ ] Base de datos migrada correctamente
- [ ] Estructuras JSON validadas
- [ ] Plantillas ML con exclusiones correctas

### 9.2 Verificaciones de Seguridad

- [ ] Credenciales por defecto cambiadas
- [ ] JWT Secret configurado (256+ bits)
- [ ] HTTPS habilitado (producción)
- [ ] Endpoints protegidos con roles

### 9.3 Verificaciones de Performance

- [ ] Índices de base de datos creados
- [ ] Consultas optimizadas
- [ ] Logs en nivel correcto (INFO/WARN)

---

## 10. Reportes de Testing

### 10.1 Generar Reporte JaCoCo

```powershell
.\mvnw.cmd test jacoco:report
```

Ubicación del reporte: `target/site/jacoco/index.html`

### 10.2 Interpretar Cobertura

| Métrica | Significado |
|---------|-------------|
| Line Coverage | % de líneas ejecutadas |
| Branch Coverage | % de branches (if/else) probados |
| Method Coverage | % de métodos con tests |

### 10.3 Métricas Recomendadas

| Módulo | Mínimo | Objetivo |
|--------|--------|----------|
| auth | 80% | 90% |
| inventario | 75% | 85% |
| estructuras | 70% | 80% |
| importacion | 75% | 85% |
| ml | 80% | 90% |

---

## Comandos Rápidos

```powershell
# Ejecutar todos los tests
.\mvnw.cmd test

# Test específico
.\mvnw.cmd test -Dtest=BienServiceTest

# Tests con cobertura
.\mvnw.cmd test jacoco:report

# Solo tests de integración
.\mvnw.cmd test -Dtest=*IntegrationTest

# Saltar tests (build rápido)
.\mvnw.cmd package -DskipTests
```

---

*© 2024 Sistema de Gestión de Inventario - UPeU*
