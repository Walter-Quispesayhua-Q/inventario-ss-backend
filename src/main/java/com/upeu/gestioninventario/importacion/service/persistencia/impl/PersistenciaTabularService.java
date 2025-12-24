package com.upeu.gestioninventario.importacion.service.persistencia.impl;

import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.estructuras.dto.estacion.BienEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.ComponenteEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.importacion.dto.FilaPreviewDTO;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class PersistenciaTabularService extends AbstractPersistenciaFormato {

    public PersistenciaTabularService(
            IResolverCategoriaService resolverCategoriaService,
            IResolverUbicacionService resolverUbicacionService,
            IResolverResponsableService resolverResponsableService,
            IBienPersistenciaService bienPersistenciaService) {
        super(resolverCategoriaService, resolverUbicacionService, 
              resolverResponsableService, bienPersistenciaService);
    }

    @Override
    public TipoFormatoDetectado getFormatoSoportado() {
        return TipoFormatoDetectado.TABULAR;
    }

    @Override
    public ResultadoPersistenciaHoja persistir(
            HojaAnalizadaDTO hoja,
            Long idUsuario,
            Persona responsableDefault,
            Departamento departamento) {

        if (!validarHoja(hoja)) {
            return ResultadoPersistenciaHoja.vacio(
                    hoja != null ? hoja.metadatos().indiceHoja() : 0,
                    hoja != null ? hoja.metadatos().nombreHoja() : "Desconocida",
                    TipoFormatoDetectado.TABULAR
            );
        }

        Set<String> seriesEnEstaciones = obtenerSeriesEnEstaciones(hoja);
        Set<String> cafsEnEstaciones = obtenerCafsEnEstaciones(hoja);

        log.info("Persistiendo hoja TABULAR: {} - {} filas (excluidas: {} por serie, {} por CAF)",
                hoja.metadatos().nombreHoja(), 
                hoja.filasEnriquecidas().size(),
                seriesEnEstaciones.size(),
                cafsEnEstaciones.size());

        ResultadoPersistenciaHoja.Builder resultado = crearBuilder(hoja);

        for (FilaPreviewDTO fila : hoja.filasEnriquecidas()) {
            if (debeExcluirFila(fila, seriesEnEstaciones, cafsEnEstaciones)) {
                log.debug("Fila {} excluida (pertenece a estación)", fila.numeroFila());
                continue;
            }
            
            if (!fila.esValida()) {
                continue;
            }
            
            persistirFila(fila, responsableDefault, departamento, resultado);
        }

        ResultadoPersistenciaHoja resultadoFinal = resultado.build();
        log.info("Hoja TABULAR '{}' completada: {} bienes, {} errores",
                hoja.metadatos().nombreHoja(),
                resultadoFinal.bienesCreados(),
                resultadoFinal.bienesFallidos());

        return resultadoFinal;
    }

    private Set<String> obtenerSeriesEnEstaciones(HojaAnalizadaDTO hoja) {
        Set<String> series = new HashSet<>();
        if (hoja.estacionesSugeridas() == null) return series;

        for (EstacionConComponentesDTO estacion : hoja.estacionesSugeridas()) {
            if (estacion.getComponentes() == null) continue;
            for (ComponenteEstacionDTO componente : estacion.getComponentes()) {
                if (componente.getBienes() == null) continue;
                for (BienEstacionDTO bien : componente.getBienes()) {
                    String serie = bien.getNumeroSerieBien();
                    if (serie != null && !serie.isBlank()) {
                        series.add(serie);
                    }
                }
            }
        }
        return series;
    }

    private Set<String> obtenerCafsEnEstaciones(HojaAnalizadaDTO hoja) {
        Set<String> cafs = new HashSet<>();
        if (hoja.estacionesSugeridas() == null) return cafs;

        for (EstacionConComponentesDTO estacion : hoja.estacionesSugeridas()) {
            if (estacion.getComponentes() == null) continue;
            for (ComponenteEstacionDTO componente : estacion.getComponentes()) {
                if (componente.getBienes() == null) continue;
                for (BienEstacionDTO bien : componente.getBienes()) {
                    String caf = bien.getCodigoBien();
                    if (caf != null && !caf.isBlank()) {
                        cafs.add(caf);
                    }
                }
            }
        }
        return cafs;
    }

    private boolean debeExcluirFila(FilaPreviewDTO fila, Set<String> seriesEnEstaciones, Set<String> cafsEnEstaciones) {
        if (fila.tieneEstacionAsociada() || fila.idComponenteAsociado() != null) {
            return true;
        }

        if (fila.atributosExtraidos() != null) {
            String serie = fila.atributosExtraidos().get("NUMERO_SERIE");
            if (serie != null && seriesEnEstaciones.contains(serie)) {
                return true;
            }
        }

        if (fila.valoresOriginales() != null) {
            String caf = obtenerCafDeFila(fila);
            if (caf != null && cafsEnEstaciones.contains(caf)) {
                return true;
            }
        }

        return false;
    }

    private String obtenerCafDeFila(FilaPreviewDTO fila) {
        if (fila.valoresOriginales() != null) {
            String caf = fila.valoresOriginales().get("CAF");
            if (caf != null && !caf.isBlank()) return caf;
            
            caf = fila.valoresOriginales().get("CÓDIGO");
            if (caf != null && !caf.isBlank()) return caf;
            
            caf = fila.valoresOriginales().get("CODIGO");
            if (caf != null && !caf.isBlank()) return caf;
        }
        
        if (fila.atributosExtraidos() != null) {
            return fila.atributosExtraidos().get("CAF");
        }
        
        return null;
    }

    private void persistirFila(
            FilaPreviewDTO fila,
            Persona responsableDefault,
            Departamento departamento,
            ResultadoPersistenciaHoja.Builder resultado) {

        Map<String, String> campos = construirCamposBase(fila);
        String caf = campos.get("CAF");
        String nombreBien = campos.get("NOMBRE_BIEN");

        try {
            Categoria categoria = resolverCategoria(fila.categoriaDetectada());
            if (categoria == null) {
                resultado.bienFallido(crearErrorBien(
                        fila.numeroFila(), caf, nombreBien,
                        "Categoría no encontrada: " + fila.categoriaDetectada()
                ));
                return;
            }

            Ubicacion ubicacion = resolverUbicacion(fila);
            Persona responsable = resolverResponsable(fila, responsableDefault);

            BienDTO bienCreado = bienPersistenciaService.guardarBien(
                    campos, categoria, responsable, ubicacion, departamento
            );

            resultado.bienCreado(new ImportacionItemResultadoDTO(
                    caf, true, "CREADO",
                    "Bien creado: " + bienCreado.nombreBien(),
                    bienCreado
            ));

            log.debug("Bien creado: {} (CAF: {})", bienCreado.nombreBien(), caf);

        } catch (Exception e) {
            log.error("Error en fila {}: {}", fila.numeroFila(), e.getMessage());
            resultado.bienFallido(crearErrorBien(fila.numeroFila(), caf, nombreBien, e.getMessage()));
        }
    }
}
