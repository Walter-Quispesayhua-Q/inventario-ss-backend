package com.upeu.gestioninventario.importacion.service.persistencia.impl;

import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.importacion.dto.FilaErrorDTO;
import com.upeu.gestioninventario.importacion.dto.FilaPreviewDTO;
import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.HojaAnalizadaDTO;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverCategoriaService;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverResponsableService;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverUbicacionService;
import com.upeu.gestioninventario.importacion.service.persistencia.IPersistenciaPorFormato;
import com.upeu.gestioninventario.importacion.dto.ResultadoPersistenciaHoja;
import com.upeu.gestioninventario.inventario.service.IBienPersistenciaService;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import com.upeu.gestioninventario.ubicaciones.model.Ubicacion;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class AbstractPersistenciaFormato implements IPersistenciaPorFormato {

    protected static final String CAF_NO_ENCONTRADO = "NO ENCONTRADO";
    protected static final String SERIE_NO_ENCONTRADA = "S/N";
    protected static final String ESTADO_FISICO_DEFAULT = "Bueno";
    protected static final String ESTADO_OPERACIONAL_DEFAULT = "Operativo";

    private static final Set<String> CLAVES_UBICACION = Set.of(
            "UBICACION", "UBICACIÓN", "UBICACION_ACTUAL", "UBICACIÓN_ACTUAL",
            "AREA_ASIGNADA", "ÁREA_ASIGNADA", "AREA ASIGNADA", "AMBIENTE"
    );

    private static final Set<String> CLAVES_RESPONSABLE = Set.of(
            "RESPONSABLE", "CUSTODIO", "ENCARGADO", "USUARIO", "ASIGNADO"
    );

    protected final IResolverCategoriaService resolverCategoriaService;
    protected final IResolverUbicacionService resolverUbicacionService;
    protected final IResolverResponsableService resolverResponsableService;
    protected final IBienPersistenciaService bienPersistenciaService;

    protected AbstractPersistenciaFormato(
            IResolverCategoriaService resolverCategoriaService,
            IResolverUbicacionService resolverUbicacionService,
            IResolverResponsableService resolverResponsableService,
            IBienPersistenciaService bienPersistenciaService) {
        this.resolverCategoriaService = resolverCategoriaService;
        this.resolverUbicacionService = resolverUbicacionService;
        this.resolverResponsableService = resolverResponsableService;
        this.bienPersistenciaService = bienPersistenciaService;
    }

    @Override
    public abstract ResultadoPersistenciaHoja persistir(
            HojaAnalizadaDTO hoja,
            Long idUsuario,
            Persona responsableDefault,
            Departamento departamento
    );

    @Override
    public abstract TipoFormatoDetectado getFormatoSoportado();

    protected boolean validarHoja(HojaAnalizadaDTO hoja) {
        if (hoja == null) {
            log.warn("Hoja nula recibida para persistencia");
            return false;
        }
        if (hoja.filasEnriquecidas() == null || hoja.filasEnriquecidas().isEmpty()) {
            log.warn("Hoja sin filas para persistir: {}", hoja.metadatos().nombreHoja());
            return false;
        }
        return true;
    }

    protected FilaErrorDTO crearErrorBien(int numeroFila, String caf, String nombreBien, String mensajeError) {
        return new FilaErrorDTO(numeroFila, caf, nombreBien, mensajeError);
    }

    protected Map<String, String> construirCamposBase(FilaPreviewDTO fila) {
        Map<String, String> campos = new HashMap<>();
        Map<String, String> valores = fila.valoresOriginales();
        Map<String, String> atributos = fila.atributosExtraidos();

        campos.put("NOMBRE_BIEN", obtenerNombreBien(valores, atributos));
        campos.put("NUMERO_SERIE", obtenerNumeroSerie(valores, atributos));
        campos.put("CAF", obtenerCaf(valores, atributos));
        campos.put("OBSERVACIONES", obtenerObservaciones(valores));
        campos.put("ESTADO_FISICO", ESTADO_FISICO_DEFAULT);
        campos.put("ESTADO_OPERACIONAL", ESTADO_OPERACIONAL_DEFAULT);

        agregarAtributosExtras(campos, atributos);

        return campos;
    }

    protected String obtenerNombreBien(Map<String, String> valores, Map<String, String> atributos) {
        String nombre = atributos.getOrDefault("NOMBRE_BIEN",
                valores.getOrDefault("BIEN",
                        valores.getOrDefault("EQUIPO",
                                valores.getOrDefault("NOMBRE_BIEN", "Sin nombre"))));
        return nombre != null && !nombre.isBlank() ? nombre : "Sin nombre";
    }

    protected String obtenerNumeroSerie(Map<String, String> valores, Map<String, String> atributos) {
        String[] clavesSerie = {
                "NUMERO_SERIE", "NUMERO SERIE", "NÚMERO SERIE", "N° SERIE",
                "SERVICE TAG / SERIE", "SERVICE TAG", "SERIE", "SERIAL"
        };
        
        if (atributos != null) {
            String serieAtributo = atributos.get("NUMERO_SERIE");
            if (serieAtributo != null && !serieAtributo.isBlank()) {
                return serieAtributo.trim();
            }
        }
        
        for (String clave : clavesSerie) {
            String valor = valores.get(clave);
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }
        
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            String claveNormalizada = entry.getKey().toLowerCase().trim();
            if (claveNormalizada.contains("serie") || claveNormalizada.contains("serial")
                    || claveNormalizada.contains("service tag")) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) {
                    return entry.getValue().trim();
                }
            }
        }
        
        return generarSerieUnica();
    }

    protected String generarSerieUnica() {
        return SERIE_NO_ENCONTRADA + "-" + System.currentTimeMillis() + "-" + 
               String.format("%04d", (int)(Math.random() * 10000));
    }

    protected String obtenerCaf(Map<String, String> valores, Map<String, String> atributos) {
        String[] clavesCAF = {"CAF", "CÓDIGO CAF", "CODIGO CAF", "CODIGO", "CÓDIGO", "ACTIVO FIJO"};
        
        for (String clave : clavesCAF) {
            String valor = valores.get(clave);
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }
        
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            String claveNormalizada = entry.getKey().toLowerCase().trim();
            if (claveNormalizada.contains("caf") || claveNormalizada.equals("codigo") 
                    || claveNormalizada.contains("activo")) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) {
                    return entry.getValue().trim();
                }
            }
        }
        
        if (atributos != null) {
            String cafAtributo = atributos.get("CAF");
            if (cafAtributo != null && !cafAtributo.isBlank()) {
                return cafAtributo.trim();
            }
        }
        
        return CAF_NO_ENCONTRADO;
    }

    protected String obtenerObservaciones(Map<String, String> valores) {
        return valores.getOrDefault("OBSERVACIONES",
                valores.getOrDefault("Observación - descargo", null));
    }

    protected void agregarAtributosExtras(Map<String, String> campos, Map<String, String> atributos) {
        if (atributos == null) return;
        atributos.forEach((key, value) -> {
            if (!campos.containsKey(key) && value != null && !value.isBlank()) {
                campos.put(key, value);
            }
        });
    }

    protected Categoria resolverCategoria(String nombreCategoria) {
        if (nombreCategoria == null || nombreCategoria.isBlank()) {
            log.warn("Nombre de categoría vacío");
            return null;
        }
        return resolverCategoriaService.resolverPorNombre(nombreCategoria).orElse(null);
    }

    protected Ubicacion resolverUbicacion(FilaPreviewDTO fila) {
        AmbienteDetectadoDTO ambiente = fila.ambienteDetectado();
        String textoUbicacion = obtenerTextoUbicacion(fila);

        return resolverUbicacionService.resolverUbicacion(
                ambiente != null ? ambiente : AmbienteDetectadoDTO.noDetectado(),
                textoUbicacion
        );
    }

    protected String obtenerTextoUbicacion(FilaPreviewDTO fila) {
        AmbienteDetectadoDTO ambiente = fila.ambienteDetectado();
        if (ambiente != null && ambiente.textoOriginalUbicacion() != null) {
            return ambiente.textoOriginalUbicacion();
        }

        if (fila.valoresOriginales() != null) {
            for (String clave : CLAVES_UBICACION) {
                String valor = fila.valoresOriginales().get(clave);
                if (valor != null && !valor.isBlank()) {
                    return valor.trim();
                }
            }
        }

        return "";
    }

    protected Persona resolverResponsable(FilaPreviewDTO fila, Persona responsableDefault) {
        String nombreResponsable = obtenerNombreResponsable(fila);

        if (nombreResponsable == null || nombreResponsable.isBlank()) {
            return responsableDefault;
        }

        return resolverResponsableService.resolverResponsableDesdeExcel(nombreResponsable);
    }

    protected String obtenerNombreResponsable(FilaPreviewDTO fila) {
        if (fila.valoresOriginales() == null) return null;

        for (String clave : CLAVES_RESPONSABLE) {
            String valor = fila.valoresOriginales().get(clave);
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }

        return null;
    }

    protected ResultadoPersistenciaHoja.Builder crearBuilder(HojaAnalizadaDTO hoja) {
        return ResultadoPersistenciaHoja.builder(
                hoja.metadatos().indiceHoja(),
                hoja.metadatos().nombreHoja(),
                hoja.metadatos().tipoFormato()
        );
    }
}
