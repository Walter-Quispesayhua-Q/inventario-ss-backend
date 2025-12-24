package com.upeu.gestioninventario.shared.services;

import com.upeu.gestioninventario.auth.model.Rol;
import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.categorias.model.TipoAtributo;
import com.upeu.gestioninventario.categorias.repository.CategoriaRepository;
import com.upeu.gestioninventario.categorias.repository.TipoAtributoRepository;
import com.upeu.gestioninventario.ml.model.PlantillaAtributo;
import com.upeu.gestioninventario.ml.model.PlantillaCategoria;
import com.upeu.gestioninventario.ml.repository.PlantillaAtributoRepository;
import com.upeu.gestioninventario.ml.repository.PlantillaCategoriaRepository;
import com.upeu.gestioninventario.ml.dto.seed.AtributoExtraccionSeedDTO;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import com.upeu.gestioninventario.inventario.repository.BienRepository;
import com.upeu.gestioninventario.ml.service.PlantillaLoaderService;
import com.upeu.gestioninventario.shared.config.properties.InicializacionProperties;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.model.UsuarioRol;
import com.upeu.gestioninventario.auth.repository.RolRepository;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.auth.repository.UsuarioRolRepository;
import com.upeu.gestioninventario.ubicaciones.repository.DepartamentoRepository;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.ubicaciones.repository.UbicacionRepository;
import com.upeu.gestioninventario.personas.repository.PersonaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Inicializador de datos base del sistema.
 * <p>
 * Se ejecuta al arranque de la aplicación creando:
 * <ul>
 *   <li>Roles y permisos básicos del sistema</li>
 *   <li>Usuarios administradores por defecto</li>
 *   <li>Departamentos y ubicaciones iniciales</li>
 *   <li>Sincronización de plantillas ML desde archivos seed</li>
 * </ul>
 * <p>
 * IMPORTANTE: Cambiar contraseñas por defecto en producción.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_USER_INTERNO = "USER_INTERNO";
    public static final String ROL_USER_OBSERVADOR = "USER_OBSERVADOR";
    
    private static final String DEPARTAMENTO_DEFAULT = "EP - Ingeniería de Sistemas";
    private static final String UBICACION_DEFAULT = "Campus UPeU Juliaca";
    
    private static final String ADMIN_EMAIL = "wali@inventario.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_CODIGO = "ADMIN-001";
    
    private static final String GESTOR_EMAIL = "gestor@inventario.com";
    private static final String GESTOR_PASSWORD = "gestor123";
    private static final String GESTOR_CODIGO = "GESTOR-001";

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRolRepository usuarioRolRepository;
    private final DepartamentoRepository departamentoRepository;
    private final PlantillaCategoriaRepository plantillaCategoriaRepository;
    private final PlantillaAtributoRepository plantillaAtributoRepository;
    private final TipoAtributoRepository tipoAtributoRepository;
    private final PlantillaLoaderService plantillaLoaderService;
    private final InicializacionProperties inicializacionProperties;
    private final CategoriaRepository categoriaRepository;
    private final BienRepository bienRepository;
    private final UbicacionRepository ubicacionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        inicializarRoles();
        inicializarDatosBase();
        
        Usuario admin = createAdminIfNotExists();
        createGestorInterinoIfNotExists();

        if (inicializacionProperties.getSincronizarPlantillas().isHabilitado()) {
            sincronizarPlantillasDesdeJson(admin);
        } else {
            log.info("Sincronización de plantillas deshabilitada");
        }
    }

    private void inicializarRoles() {
        createRoleIfNotExists(ROL_ADMIN, "Rol de administrador con acceso completo al sistema");
        createRoleIfNotExists(ROL_USER_INTERNO, "Rol para empleados que gestionan el inventario");
        createRoleIfNotExists(ROL_USER_OBSERVADOR, "Rol de observador con acceso de solo lectura");
        log.info("Roles inicializados correctamente");
    }

    private void inicializarDatosBase() {
        createDefaultDepartmentIfNotExists();
        createDefaultUbicacionIfNotExists();
    }

    private void createRoleIfNotExists(String nombreRol, String descripcion) {
        if (rolRepository.findByNombreRol(nombreRol).isEmpty()) {
            Rol nuevoRol = Rol.builder()
                    .nombreRol(nombreRol)
                    .descripcion(descripcion)
                    .build();
            rolRepository.save(nuevoRol);
            log.info("Rol creado: {}", nombreRol);
        }
    }

    private void createDefaultDepartmentIfNotExists() {
        if (departamentoRepository.findByNombreDepartamento(DEPARTAMENTO_DEFAULT).isEmpty()) {
            Departamento departamento = Departamento.builder()
                    .nombreDepartamento(DEPARTAMENTO_DEFAULT)
                    .descripcion("Departamento por defecto para usuarios del sistema")
                    .build();
            departamentoRepository.save(departamento);
            log.info("Departamento por defecto creado: {}", DEPARTAMENTO_DEFAULT);
        }
    }

    private void createDefaultUbicacionIfNotExists() {
        if (ubicacionRepository.findByNombreUbicacion(UBICACION_DEFAULT).isEmpty()) {
            Ubicacion ubicacion = Ubicacion.builder()
                    .nombreUbicacion(UBICACION_DEFAULT)
                    .descripcion("Ubicación raíz para toda la institución")
                    .build();
            ubicacionRepository.save(ubicacion);
            log.info("Ubicación por defecto creada: {}", UBICACION_DEFAULT);
        }
    }

    private Usuario createAdminIfNotExists() {
        if (!usuarioRepository.existsByRol(ROL_ADMIN)) {
            log.info("Creando usuario administrador por defecto");

            Persona personaAdmin = crearPersona("admin", "Principal", ADMIN_EMAIL, ADMIN_CODIGO);
            Departamento departamento = obtenerDepartamentoDefault();
            Usuario usuarioAdmin = crearUsuario(ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN_CODIGO, departamento, personaAdmin);
            asignarRol(usuarioAdmin, ROL_ADMIN);

            log.warn("ADMIN creado - Email: {} | Password: {} - ¡CAMBIAR EN PRODUCCIÓN!", 
                    ADMIN_EMAIL, ADMIN_PASSWORD);
            return usuarioAdmin;
        }
        
        return usuarioRepository.findByEmail(ADMIN_EMAIL)
                .orElseThrow(() -> new RuntimeException("Usuario admin no encontrado"));
    }

    private Usuario createGestorInterinoIfNotExists() {
        if (!usuarioRepository.existsByRol(ROL_USER_INTERNO)) {
            log.info("Creando usuario gestor interino por defecto");

            Persona personaGestor = crearPersona("Gestor", "Interino", GESTOR_EMAIL, GESTOR_CODIGO);
            Departamento departamento = obtenerDepartamentoDefault();
            Usuario usuarioGestor = crearUsuario(GESTOR_EMAIL, GESTOR_PASSWORD, GESTOR_CODIGO, departamento, personaGestor);
            asignarRol(usuarioGestor, ROL_USER_INTERNO);

            log.warn("GESTOR creado - Email: {} | Password: {} - ¡CAMBIAR EN PRODUCCIÓN!", 
                    GESTOR_EMAIL, GESTOR_PASSWORD);
            return usuarioGestor;
        }
        
        return usuarioRepository.findByEmail(GESTOR_EMAIL)
                .orElseThrow(() -> new RuntimeException("Usuario gestor no encontrado"));
    }

    private Persona crearPersona(String nombre, String apellido, String email, String codigo) {
        Persona persona = Persona.builder()
                .nombre(nombre)
                .apellido(apellido)
                .email(email)
                .codigo(codigo)
                .build();
        return personaRepository.save(persona);
    }

    private Usuario crearUsuario(String email, String password, String codigo, 
                                 Departamento departamento, Persona persona) {
        Usuario usuario = Usuario.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .codigoUsuario(codigo)
                .activo(true)
                .departamento(departamento)
                .persona(persona)
                .build();
        return usuarioRepository.save(usuario);
    }

    private void asignarRol(Usuario usuario, String nombreRol) {
        Rol rol = rolRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nombreRol));
        
        UsuarioRol usuarioRol = UsuarioRol.builder()
                .usuario(usuario)
                .rol(rol)
                .build();
        usuarioRolRepository.save(usuarioRol);
    }

    private Departamento obtenerDepartamentoDefault() {
        return departamentoRepository.findByNombreDepartamento(DEPARTAMENTO_DEFAULT)
                .orElseThrow(() -> new RuntimeException("Departamento por defecto no encontrado: " + DEPARTAMENTO_DEFAULT));
    }

    private void sincronizarPlantillasDesdeJson(Usuario creador) {
        Map<String, PlantillaSeedDTO> plantillasCargadas = plantillaLoaderService.getPlantillasCache();
        Set<String> plantillasCambiadas = plantillaLoaderService.getPlantillasCambiadas();

        if (plantillasCargadas.isEmpty()) {
            log.info("No hay plantillas en caché, omitiendo sincronización");
            return;
        }

        if (plantillasCambiadas.isEmpty()) {
            log.info("Sin cambios detectados en plantillas");
            return;
        }

        List<PlantillaSeedDTO> plantillasParaSincronizar = plantillasCargadas.entrySet().stream()
                .filter(entry -> plantillasCambiadas.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        log.info("Sincronizando {} plantillas de {} totales", 
                plantillasParaSincronizar.size(), plantillasCargadas.size());
        
        try {
            procesarPlantillas(plantillasParaSincronizar, creador);
            log.info("Sincronización completada: {} plantillas procesadas", plantillasParaSincronizar.size());
        } catch (Exception e) {
            log.error("Error en sincronización de plantillas", e);
        }
    }

    private void procesarPlantillas(List<PlantillaSeedDTO> plantillasSeed, Usuario creador) {
        Set<AtributoExtraccionSeedDTO> todosLosAtributos = extraerAtributosUnicos(plantillasSeed);
        crearAtributosFaltantes(todosLosAtributos);

        log.info("Sincronizando {} plantillas", plantillasSeed.size());
        for (PlantillaSeedDTO seedData : plantillasSeed) {
            try {
                sincronizarPlantilla(seedData, creador);
            } catch (Exception e) {
                log.error("Error sincronizando plantilla '{}' ({})", 
                        seedData.metadatos().nombre(), seedData.metadatos().idPlantilla(), e);
            }
        }
    }

    private Set<AtributoExtraccionSeedDTO> extraerAtributosUnicos(List<PlantillaSeedDTO> plantillasSeed) {
        Set<AtributoExtraccionSeedDTO> atributos = new HashSet<>();
        plantillasSeed.forEach(p -> atributos.addAll(p.atributosExtraccion()));
        return atributos;
    }

    private void crearAtributosFaltantes(Set<AtributoExtraccionSeedDTO> atributos) {
        Set<String> atributosExistentes = new HashSet<>(tipoAtributoRepository.findAllNombresAtributo());
        log.debug("Tipos de atributo existentes en BD: {}", atributosExistentes.size());

        for (AtributoExtraccionSeedDTO atributo : atributos) {
            if (!atributosExistentes.contains(atributo.nombreCampo())) {
                crearTipoAtributo(atributo);
                atributosExistentes.add(atributo.nombreCampo());
            }
        }
    }

    private void crearTipoAtributo(AtributoExtraccionSeedDTO attrDTO) {
        TipoAtributo atributo = TipoAtributo.builder()
                .nombreAtributo(attrDTO.nombreCampo())
                .tipoDato(attrDTO.tipoDato())
                .build();
        tipoAtributoRepository.save(atributo);
        log.debug("TipoAtributo creado: {}", attrDTO.nombreCampo());
    }

    private void sincronizarPlantilla(PlantillaSeedDTO seedData, Usuario creador) {
        PlantillaCategoria plantilla = obtenerOCrearPlantilla(seedData, creador);
        actualizarDatosPlantilla(plantilla, seedData, creador);
        PlantillaCategoria plantillaGuardada = plantillaCategoriaRepository.save(plantilla);

        crearCategoriaBaseSiNoExiste(seedData.metadatos().nombre(), creador);
        sincronizarAtributosPlantilla(plantillaGuardada, seedData.atributosExtraccion());

        log.info("Plantilla sincronizada: '{}' con {} atributos", 
                plantillaGuardada.getNombrePlantilla(), seedData.atributosExtraccion().size());
    }

    private PlantillaCategoria obtenerOCrearPlantilla(PlantillaSeedDTO seedData, Usuario creador) {
        return plantillaCategoriaRepository.findByIdPlantillaSeed(seedData.metadatos().idPlantilla())
                .map(existente -> {
                    existente.setUsuarioUltimaModificacion(creador);
                    return existente;
                })
                .orElseGet(() -> {
                    PlantillaCategoria nueva = new PlantillaCategoria();
                    nueva.setUsuarioCreacion(creador);
                    nueva.setUsuarioUltimaModificacion(creador);
                    return nueva;
                });
    }

    private void actualizarDatosPlantilla(PlantillaCategoria plantilla, PlantillaSeedDTO seedData, Usuario creador) {
        plantilla.setIdPlantillaSeed(seedData.metadatos().idPlantilla());
        plantilla.setNombrePlantilla(seedData.metadatos().nombre());
        plantilla.setVersion(seedData.metadatos().version());
        plantilla.setDescripcion(seedData.metadatos().descripcion());
        plantilla.setCategoriaSemantica(seedData.metadatos().categoriaSemantica());
        plantilla.setJsonScoring(seedData.scoring());
        plantilla.setJsonReconocimiento(seedData.reconocimiento());
        plantilla.setJsonPatrones(seedData.patronesRegex());
        plantilla.setJsonInteligencia(seedData.inteligencia());
        plantilla.setJsonAprendizaje(seedData.aprendizaje());
    }

    private void crearCategoriaBaseSiNoExiste(String nombreCategoria, Usuario creador) {
        categoriaRepository.findByNombreCategoria(nombreCategoria)
                .orElseGet(() -> {
                    Categoria nuevaCategoria = Categoria.builder()
                            .nombreCategoria(nombreCategoria)
                            .descripcion("Categoría creada automáticamente desde plantilla")
                            .estado(true)
                            .visible(false)
                            .esInteligente(true)
                            .usuarioCreacion(creador)
                            .usuarioUltimaModificacion(creador)
                            .build();
                    return categoriaRepository.save(nuevaCategoria);
                });
    }

    private void sincronizarAtributosPlantilla(PlantillaCategoria plantilla, 
                                              List<AtributoExtraccionSeedDTO> atributos) {
        for (AtributoExtraccionSeedDTO attrDTO : atributos) {
            TipoAtributo tipoAtributo = tipoAtributoRepository.findByNombreAtributo(attrDTO.nombreCampo())
                    .orElseThrow(() -> new RuntimeException("TipoAtributo no encontrado: " + attrDTO.nombreCampo()));

            PlantillaAtributo plantillaAtributo = obtenerOCrearPlantillaAtributo(plantilla, tipoAtributo);
            actualizarDatosPlantillaAtributo(plantillaAtributo, attrDTO, plantilla, tipoAtributo);
            plantillaAtributoRepository.save(plantillaAtributo);
        }
    }

    private PlantillaAtributo obtenerOCrearPlantillaAtributo(PlantillaCategoria plantilla, 
                                                             TipoAtributo tipoAtributo) {
        return plantillaAtributoRepository
                .findByPlantillaAndTipoAtributo(plantilla, tipoAtributo)
                .orElse(new PlantillaAtributo());
    }

    private void actualizarDatosPlantillaAtributo(PlantillaAtributo plantillaAtributo, 
                                                  AtributoExtraccionSeedDTO attrDTO,
                                                  PlantillaCategoria plantilla, 
                                                  TipoAtributo tipoAtributo) {
        plantillaAtributo.setPlantilla(plantilla);
        plantillaAtributo.setTipoAtributo(tipoAtributo);
        plantillaAtributo.setEtiqueta(attrDTO.etiqueta());
        plantillaAtributo.setEsRequerido(attrDTO.requerido() != null && attrDTO.requerido());
        plantillaAtributo.setOrdenUI(attrDTO.ordenUI());
        plantillaAtributo.setPlaceholder(attrDTO.placeholder());
        plantillaAtributo.setJsonExtraccion(attrDTO.extraccion());
        plantillaAtributo.setJsonValidacion(attrDTO.validacion());
        
        if (attrDTO.opciones() != null) {
            plantillaAtributo.setJsonOpciones(Map.of("opciones", attrDTO.opciones()));
        } else {
            plantillaAtributo.setJsonOpciones(null);
        }
    }
}
