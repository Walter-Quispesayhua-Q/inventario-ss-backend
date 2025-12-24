package com.upeu.gestioninventario.importacion.service.persistencia.impl;

import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.estructuras.dto.estacion.*;
import com.upeu.gestioninventario.estructuras.model.NivelEstructura;
import com.upeu.gestioninventario.estructuras.model.TipoEstructuraEntity;
import com.upeu.gestioninventario.estructuras.repository.TipoEstructuraRepository;
import com.upeu.gestioninventario.estructuras.service.IComponenteService;
import com.upeu.gestioninventario.estructuras.service.IEstacionService;
import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.importacion.dto.ImportacionItemResultadoDTO;
import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.HojaAnalizadaDTO;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverCategoriaService;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverResponsableService;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverUbicacionService;
import com.upeu.gestioninventario.importacion.dto.ResultadoPersistenciaHoja;
import com.upeu.gestioninventario.inventario.dto.bien.BienDTO;
import com.upeu.gestioninventario.inventario.service.IBienPersistenciaService;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PersistenciaMatrizService extends AbstractPersistenciaFormato {

    private final IEstacionService estacionService;
    private final IComponenteService componenteService;
    private final TipoEstructuraRepository tipoEstructuraRepository;

    public PersistenciaMatrizService(
            IResolverCategoriaService resolverCategoriaService,
            IResolverUbicacionService resolverUbicacionService,
            IResolverResponsableService resolverResponsableService,
            IBienPersistenciaService bienPersistenciaService,
            IEstacionService estacionService,
            IComponenteService componenteService,
            TipoEstructuraRepository tipoEstructuraRepository) {
        super(resolverCategoriaService, resolverUbicacionService,
              resolverResponsableService, bienPersistenciaService);
        this.estacionService = estacionService;
        this.componenteService = componenteService;
        this.tipoEstructuraRepository = tipoEstructuraRepository;
    }

    @Override
    public TipoFormatoDetectado getFormatoSoportado() {
        return TipoFormatoDetectado.MATRIZ;
    }

    @Override
    public ResultadoPersistenciaHoja persistir(
            HojaAnalizadaDTO hoja,
            Long idUsuario,
            Persona responsableDefault,
            Departamento departamento) {

        if (hoja == null) {
            return ResultadoPersistenciaHoja.vacio(0, "Desconocida", TipoFormatoDetectado.MATRIZ);
        }

        log.info("Persistiendo hoja MATRIZ: {} - {} estaciones",
                hoja.metadatos().nombreHoja(),
                hoja.estacionesSugeridas() != null ? hoja.estacionesSugeridas().size() : 0);

        ResultadoPersistenciaHoja.Builder resultado = crearBuilder(hoja);

        if (hoja.estacionesSugeridas() == null || hoja.estacionesSugeridas().isEmpty()) {
            log.warn("Hoja MATRIZ sin estaciones sugeridas");
            return resultado.build();
        }

        for (EstacionConComponentesDTO estacion : hoja.estacionesSugeridas()) {
            persistirEstacion(estacion, idUsuario, responsableDefault, departamento, resultado);
        }

        ResultadoPersistenciaHoja resultadoFinal = resultado.build();
        log.info("Hoja MATRIZ '{}' completada: {} estaciones, {} bienes",
                hoja.metadatos().nombreHoja(),
                resultadoFinal.estacionesCreadas(),
                resultadoFinal.bienesCreados());

        return resultadoFinal;
    }

    private void persistirEstacion(
            EstacionConComponentesDTO estacionDTO,
            Long idUsuario,
            Persona responsableDefault,
            Departamento departamento,
            ResultadoPersistenciaHoja.Builder resultado) {

        String nombreEstacion = estacionDTO.getNombre() != null ? estacionDTO.getNombre() : "Sin nombre";
        String codigoEstacion = estacionDTO.getCodigo();

        if (estacionDTO.getIdAmbiente() == null) {
            String errorMsg = String.format("Ambiente '%s' no encontrado en el sistema", 
                    estacionDTO.getNombreAmbiente() != null ? estacionDTO.getNombreAmbiente() : "desconocido");
            log.warn("Estación '{}' sin ambiente detectado: {}", nombreEstacion, errorMsg);
            resultado.estacionFallida(crearErrorBien(0, codigoEstacion, nombreEstacion, errorMsg));
            return;
        }

        try {
            // Resolver responsable ANTES de crear estación
            Persona responsable = resolverResponsableEstacion(estacionDTO, responsableDefault);
            
            // Pasar responsable al crear estación
            EstacionDTO estacionCreada = crearEstacion(estacionDTO, responsable);
            if (estacionCreada == null) {
                resultado.estacionFallida(crearErrorBien(0, codigoEstacion, nombreEstacion, 
                        "No se pudo crear la estación - verifique los datos del archivo"));
                return;
            }

            resultado.estacionCreada();

            Ubicacion ubicacion = resolverUbicacionEstacion(estacionDTO);
            // El responsable ya se resolvió arriba, se reutiliza

            persistirComponentes(
                    estacionDTO, estacionCreada.getIdEstacion(),
                    idUsuario, ubicacion, responsable, departamento, resultado
            );

        } catch (Exception e) {
            log.error("Error creando estación '{}': {}", nombreEstacion, e.getMessage());
            resultado.estacionFallida(crearErrorBien(0, codigoEstacion, nombreEstacion, e.getMessage()));
        }
    }

    private EstacionDTO crearEstacion(
            EstacionConComponentesDTO estacionDTO,
            Persona responsable) {
        
        // Obtener tipo de estructura por defecto para ESTACION
        Long idTipoEstructura = tipoEstructuraRepository
                .findByNivelAplicableAndActivoTrue(NivelEstructura.ESTACION)
                .stream()
                .findFirst()
                .map(TipoEstructuraEntity::getIdTipo)
                .orElse(null);
        
        if (idTipoEstructura == null) {
            log.error("No hay tipos de estructura activos para nivel ESTACION");
            return null;
        }
        
        EstacionInputDTO input = EstacionInputDTO.builder()
                .nombre(estacionDTO.getNombre())
                .codigo(estacionDTO.getCodigo())
                .idAmbiente(estacionDTO.getIdAmbiente())
                .idTipoEstructura(idTipoEstructura)
                .capacidadBienes(estacionDTO.getTotalBienes())
                .idResponsable(responsable != null ? responsable.getId() : null)
                .build();
        
        var resultado = estacionService.crearEstacion(input);
        if (!resultado.getExito() || resultado.getData() == null) {
            log.error("Error al crear estación: {}", resultado.getMensaje());
            return null;
        }
        return resultado.getData();
    }

    private void persistirComponentes(
            EstacionConComponentesDTO estacionDTO,
            Long idEstacion,
            Long idUsuario,
            Ubicacion ubicacion,
            Persona responsable,
            Departamento departamento,
            ResultadoPersistenciaHoja.Builder resultado) {

        if (estacionDTO.getComponentes() == null) return;

        for (ComponenteEstacionDTO componenteDTO : estacionDTO.getComponentes()) {
            ComponenteEstacionDTO componenteCreado = crearComponente(componenteDTO, idEstacion);
            if (componenteCreado == null || componenteDTO.getBienes() == null) continue;

            for (BienEstacionDTO bienDTO : componenteDTO.getBienes()) {
                persistirBienDeComponente(
                        bienDTO, componenteCreado.getId(), componenteDTO.getCategoriaBase(),
                        idUsuario, ubicacion, responsable, departamento, resultado
                );
            }
        }
    }

    private ComponenteEstacionDTO crearComponente(ComponenteEstacionDTO dto, Long idEstacion) {
        try {
            ComponenteEstacionInputDTO input = ComponenteEstacionInputDTO.builder()
                    .idEstacion(idEstacion)
                    .nombre(dto.getNombre())
                    .tipo(dto.getTipo())
                    .categoriaBase(dto.getCategoriaBase())
                    .descripcion(dto.getDescripcion())
                    .orden(dto.getOrden())
                    .build();

            var resultado = componenteService.crearComponente(input);
            if (!resultado.getExito() || resultado.getData() == null) {
                log.error("Error creando componente '{}': {}", dto.getNombre(), resultado.getMensaje());
                return null;
            }
            return resultado.getData();
        } catch (Exception e) {
            log.error("Error creando componente '{}': {}", dto.getNombre(), e.getMessage());
            return null;
        }
    }

    private void persistirBienDeComponente(
            BienEstacionDTO bienDTO,
            Long idComponente,
            String categoriaBase,
            Long idUsuario,
            Ubicacion ubicacion,
            Persona responsable,
            Departamento departamento,
            ResultadoPersistenciaHoja.Builder resultado) {

        Map<String, String> campos = construirCamposDesdeBienEstacion(bienDTO);
        String caf = campos.get("CAF");
        String nombreBien = campos.get("NOMBRE_BIEN");

        try {
            Categoria categoria = resolverCategoriaParaBien(bienDTO, categoriaBase);
            if (categoria == null) {
                resultado.bienFallido(crearErrorBien(0, caf, nombreBien,
                        "Categoría no encontrada para: " + bienDTO.getNombreBien()));
                return;
            }

            BienDTO bienCreado = bienPersistenciaService.guardarBien(
                    campos, categoria, responsable, ubicacion, departamento
            );

            componenteService.agregarBienAComponente(idComponente, bienCreado.id(), idUsuario);

            resultado.bienCreado(new ImportacionItemResultadoDTO(
                    caf, true, "CREADO",
                    "Bien vinculado a componente: " + bienCreado.nombreBien(),
                    bienCreado
            ));

        } catch (Exception e) {
            log.error("Error creando bien '{}': {}", bienDTO.getNombreBien(), e.getMessage());
            resultado.bienFallido(crearErrorBien(0, caf, nombreBien, e.getMessage()));
        }
    }

    private Map<String, String> construirCamposDesdeBienEstacion(BienEstacionDTO bienDTO) {
        Map<String, String> campos = new HashMap<>();

        campos.put("NOMBRE_BIEN", bienDTO.getNombreBien() != null ? bienDTO.getNombreBien() : "Sin nombre");

        String serial = bienDTO.getNumeroSerieBien();
        campos.put("NUMERO_SERIE", serial != null && !serial.isBlank() ? serial : generarSerieUnica());

        String caf = bienDTO.getCodigoBien();
        campos.put("CAF", caf != null && !caf.isBlank() ? caf : CAF_NO_ENCONTRADO);

        campos.put("OBSERVACIONES", bienDTO.getObservaciones());
        campos.put("ESTADO_FISICO", ESTADO_FISICO_DEFAULT);
        campos.put("ESTADO_OPERACIONAL", ESTADO_OPERACIONAL_DEFAULT);

        // Agregar atributos dinámicos (MARCA, MODELO, TIPO, etc.)
        agregarAtributosExtras(campos, bienDTO.getAtributosExtraidos());

        return campos;
    }

    private Categoria resolverCategoriaParaBien(BienEstacionDTO bienDTO, String categoriaBase) {
        if (bienDTO.getCategoriaDetectada() != null && !bienDTO.getCategoriaDetectada().isBlank()) {
            Categoria cat = resolverCategoria(bienDTO.getCategoriaDetectada());
            if (cat != null) return cat;
        }

        if (categoriaBase != null && !categoriaBase.isBlank()) {
            return resolverCategoria(categoriaBase);
        }

        return resolverCategoria(bienDTO.getNombreBien());
    }

    private Ubicacion resolverUbicacionEstacion(EstacionConComponentesDTO estacion) {
        return resolverUbicacionService.resolverUbicacion(
                new AmbienteDetectadoDTO(
                        estacion.getIdAmbiente(),
                        estacion.getNombreAmbiente(),
                        null, null,
                        estacion.getNombrePiso(),
                        null, null,
                        estacion.getNombreEdificio(),
                        1.0,
                        estacion.getNombreAmbiente()
                ),
                estacion.getNombreAmbiente()
        );
    }

    private Persona resolverResponsableEstacion(EstacionConComponentesDTO estacion, Persona defaultResp) {
        String responsable = estacion.getResponsableDetectado();
        if (responsable != null && !responsable.isBlank()) {
            return resolverResponsableService.resolverResponsableDesdeExcel(responsable);
        }
        return defaultResp;
    }
}
