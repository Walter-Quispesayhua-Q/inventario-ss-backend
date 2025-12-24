package com.upeu.gestioninventario.estructuras.service.impl;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.auth.repository.UsuarioRepository;
import com.upeu.gestioninventario.configuracion.service.ISeedStateManager;
import com.upeu.gestioninventario.estructuras.dto.seed.CampusConfigSeedDTO;
import com.upeu.gestioninventario.estructuras.dto.seed.EdificioSeedDTO;
import com.upeu.gestioninventario.estructuras.model.*;
import com.upeu.gestioninventario.estructuras.repository.AmbienteRepository;
import com.upeu.gestioninventario.estructuras.repository.EdificioRepository;
import com.upeu.gestioninventario.estructuras.repository.PisoRepository;
import com.upeu.gestioninventario.estructuras.repository.TipoEstructuraRepository;
import com.upeu.gestioninventario.shared.utils.hash.IHashCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstructurasSeederService {

    private static final String TIPO_SEED = "ESTRUCTURAS";

    private final TipoEstructuraRepository tipoEstructuraRepository;
    private final EdificioRepository edificioRepository;
    private final PisoRepository pisoRepository;
    private final AmbienteRepository ambienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;
    private final ISeedStateManager seedStateManager;
    private final IHashCalculator hashCalculator;

    @Value("${app.seed.estructuras.enabled:true}")
    private boolean seedEnabled;

    private final Map<String, Long> tiposMap = new HashMap<>();
    private Usuario usuarioAdmin;

    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    @Transactional
    public void inicializarEstructuras() {
        if (!seedEnabled) {
            log.info("Seed de estructuras deshabilitado");
            return;
        }

        usuarioAdmin = usuarioRepository.findByEmail("wali@inventario.com")
                .orElseThrow(() -> new RuntimeException("Usuario admin no encontrado"));

        configurarSecurityContext();

        try {
            long inicio = System.currentTimeMillis();

            cargarTiposEstructura();
            int[] stats = cargarEdificiosDesdeArchivos();

            if (stats[0] > 0 || stats[1] > 0 || stats[2] > 0) {
                log.info("Seed completado en {} ms: {} edificios, {} pisos, {} ambientes",
                        System.currentTimeMillis() - inicio, stats[0], stats[1], stats[2]);
            } else {
                log.info("Estructuras sin cambios - sistema de hash no detecto modificaciones");
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void configurarSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken(usuarioAdmin, null, usuarioAdmin.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void cargarTiposEstructura() {
        crearTipo("Pabellón Académico", "PAB-ACAD", NivelEstructura.EDIFICIO, "building", "#3498db");
        crearTipo("Edificio Especial", "EDIF-ESP", NivelEstructura.EDIFICIO, "landmark", "#9b59b6");
        crearTipo("Piso Estándar", "PISO-STD", NivelEstructura.PISO, "layer-group", "#2ecc71");
        crearTipo("Aula Teórica", "AMB-AULA", NivelEstructura.AMBIENTE, "chalkboard", "#f39c12");
        crearTipo("Laboratorio", "AMB-LAB", NivelEstructura.AMBIENTE, "flask", "#e74c3c");
        crearTipo("Auditorio", "AMB-AUD", NivelEstructura.AMBIENTE, "theater-masks", "#1abc9c");
        crearTipo("Oficina", "AMB-OFIC", NivelEstructura.AMBIENTE, "briefcase", "#34495e");
        crearTipo("Escritorio", "EST-ESCR", NivelEstructura.ESTACION, "desk", "#95a5a6");
        crearTipo("Mesa de Laboratorio", "EST-MESA-LAB", NivelEstructura.ESTACION, "table", "#e67e22");
        crearTipo("Pupitre", "EST-PUPITRE", NivelEstructura.ESTACION, "chair", "#16a085");

        log.info("Tipos de estructura cargados: {}", tiposMap.size());
    }

    private void crearTipo(String nombre, String codigo, NivelEstructura nivel, String icono, String color) {
        tipoEstructuraRepository.findByCodigo(codigo).ifPresentOrElse(
                tipo -> tiposMap.put(codigo, tipo.getIdTipo()),
                () -> {
                    TipoEstructuraEntity tipo = new TipoEstructuraEntity();
                    tipo.setNombre(nombre);
                    tipo.setCodigo(codigo);
                    tipo.setNivelAplicable(nivel);
                    tipo.setIcono(icono);
                    tipo.setColor(color);
                    tipo.setActivo(true);
                    TipoEstructuraEntity guardado = tipoEstructuraRepository.save(tipo);
                    tiposMap.put(codigo, guardado.getIdTipo());
                }
        );
    }

    private int[] cargarEdificiosDesdeArchivos() throws RuntimeException {
        try {
            CampusConfigSeedDTO config = cargarConfigCampus();
            if (config == null) {
                log.warn("No se encontro configuracion de campus");
                return new int[]{0, 0, 0};
            }

            log.info("Cargando estructuras para: {} - {}",
                    config.campus().universidad(), config.campus().nombre());

            Resource[] archivosEdificios = buscarArchivosEdificios(config.configuracion().carpetaEdificios());

            int edificios = 0, pisos = 0, ambientes = 0;
            int edificiosCambiados = 0;

            for (Resource archivo : archivosEdificios) {
                String nombreArchivo = archivo.getFilename();
                if (nombreArchivo == null) continue;

                String hashActual = hashCalculator.calcularHash(archivo);
                boolean necesitaRecarga = seedStateManager.necesitaRecarga(nombreArchivo, hashActual);

                if (!necesitaRecarga) {
                    log.debug("Edificio sin cambios, omitiendo: {}", nombreArchivo);
                    continue;
                }

                EdificioSeedDTO edificioSeed = objectMapper.readValue(archivo.getInputStream(), EdificioSeedDTO.class);
                int[] stats = procesarEdificio(edificioSeed.edificio());
                edificios++;
                edificiosCambiados++;
                pisos += stats[0];
                ambientes += stats[1];

                String rutaArchivo = "seed/estructuras/" + config.configuracion().carpetaEdificios() + "/" + nombreArchivo;
                seedStateManager.registrarCarga(nombreArchivo, rutaArchivo, TIPO_SEED, hashActual);

                log.info("Cargado: {} ({} pisos, {} ambientes)", 
                        edificioSeed.edificio().codigo(), stats[0], stats[1]);
            }

            if (edificiosCambiados == 0 && archivosEdificios.length > 0) {
                log.info("Todos los edificios estan actualizados, ninguno requiere recarga");
            }

            return new int[]{edificios, pisos, ambientes};
        } catch (IOException e) {
            throw new RuntimeException("Error cargando estructuras", e);
        }
    }

    private CampusConfigSeedDTO cargarConfigCampus() throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        Resource config = resolver.getResource("classpath:seed/estructuras/_config/campus-upeu-juliaca.json");

        if (!config.exists()) return null;

        return objectMapper.readValue(config.getInputStream(), CampusConfigSeedDTO.class);
    }

    private Resource[] buscarArchivosEdificios(String carpeta) throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        return resolver.getResources("classpath:seed/estructuras/" + carpeta + "/*.json");
    }

    private int[] procesarEdificio(EdificioSeedDTO.EdificioData data) {
        Edificio edificio = Edificio.builder()
                .nombre(data.nombre())
                .codigo(data.codigo())
                .tipoEstructura(obtenerTipo(data.tipoEstructura()))
                .direccion(data.direccion())
                .numeroPisos(data.numeroPisos())
                .areaTotalM2(data.areaTotalM2())
                .anoConstruccion(data.anoConstruccion())
                .responsableMantenimiento(obtenerResponsable(data.idResponsableMantenimiento()))
                .observaciones(data.observaciones())
                .build();

        edificioRepository.save(edificio);

        int pisos = 0, ambientes = 0;

        if (data.pisos() != null) {
            for (EdificioSeedDTO.PisoData pisoData : data.pisos()) {
                Piso piso = procesarPiso(pisoData, edificio);
                pisos++;

                if (pisoData.ambientes() != null) {
                    for (EdificioSeedDTO.AmbienteData ambienteData : pisoData.ambientes()) {
                        procesarAmbiente(ambienteData, piso);
                        ambientes++;
                    }
                }
            }
        }

        return new int[]{pisos, ambientes};
    }

    private Piso procesarPiso(EdificioSeedDTO.PisoData data, Edificio edificio) {
        Piso piso = Piso.builder()
                .nombre(data.nombre())
                .codigo(data.codigo())
                .numeroPiso(data.numeroPiso())
                .tipoEstructura(obtenerTipo(data.tipoEstructura()))
                .edificio(edificio)
                .responsableMantenimiento(obtenerResponsable(data.idResponsableMantenimiento()))
                .observaciones(data.observaciones())
                .build();

        return pisoRepository.save(piso);
    }

    private void procesarAmbiente(EdificioSeedDTO.AmbienteData data, Piso piso) {
        Ambiente ambiente = Ambiente.builder()
                .nombre(data.nombre())
                .codigo(data.codigo())
                .tipoEstructura(obtenerTipo(data.tipoEstructura()))
                .piso(piso)
                .capacidadPersonas(data.capacidadPersonas())
                .responsable(obtenerResponsable(data.idResponsable()))
                .observaciones(data.observaciones())
                .palabrasClaveUbicacion(convertirPalabrasClave(data.palabrasClaveUbicacion()))
                .build();

        ambienteRepository.save(ambiente);
    }

    private TipoEstructuraEntity obtenerTipo(String codigo) {
        Long id = tiposMap.get(codigo);
        if (id == null) throw new IllegalStateException("Tipo no encontrado: " + codigo);
        return tipoEstructuraRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Tipo no existe: " + codigo));
    }

    private Usuario obtenerResponsable(Long id) {
        if (id == null) return usuarioAdmin;
        return usuarioRepository.findById(id).orElse(usuarioAdmin);
    }

    private String convertirPalabrasClave(List<String> palabras) {
        if (palabras == null || palabras.isEmpty()) return null;
        return String.join(",", palabras).toLowerCase();
    }
}
