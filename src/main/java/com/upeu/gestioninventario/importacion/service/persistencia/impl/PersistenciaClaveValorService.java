package com.upeu.gestioninventario.importacion.service.persistencia.impl;

import com.upeu.gestioninventario.categorias.model.Categoria;
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

import java.util.Map;

@Service
@Slf4j
public class PersistenciaClaveValorService extends AbstractPersistenciaFormato {

    public PersistenciaClaveValorService(
            IResolverCategoriaService resolverCategoriaService,
            IResolverUbicacionService resolverUbicacionService,
            IResolverResponsableService resolverResponsableService,
            IBienPersistenciaService bienPersistenciaService) {
        super(resolverCategoriaService, resolverUbicacionService,
              resolverResponsableService, bienPersistenciaService);
    }

    @Override
    public TipoFormatoDetectado getFormatoSoportado() {
        return TipoFormatoDetectado.CLAVE_VALOR;
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
                    TipoFormatoDetectado.CLAVE_VALOR
            );
        }

        log.info("Persistiendo hoja CLAVE_VALOR: {} - {} filas",
                hoja.metadatos().nombreHoja(), hoja.filasEnriquecidas().size());

        ResultadoPersistenciaHoja.Builder resultado = crearBuilder(hoja);

        for (FilaPreviewDTO fila : hoja.filasEnriquecidas()) {
            if (!fila.esValida()) {
                continue;
            }
            persistirFilaClaveValor(fila, responsableDefault, departamento, resultado);
        }

        ResultadoPersistenciaHoja resultadoFinal = resultado.build();
        log.info("Hoja CLAVE_VALOR '{}' completada: {} bienes, {} errores",
                hoja.metadatos().nombreHoja(),
                resultadoFinal.bienesCreados(),
                resultadoFinal.bienesFallidos());

        return resultadoFinal;
    }

    private void persistirFilaClaveValor(
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
                    "Bien creado desde clave-valor: " + bienCreado.nombreBien(),
                    bienCreado
            ));

            log.debug("Bien creado (clave-valor): {} (CAF: {})", bienCreado.nombreBien(), caf);

        } catch (Exception e) {
            log.error("Error en fila clave-valor {}: {}", fila.numeroFila(), e.getMessage());
            resultado.bienFallido(crearErrorBien(fila.numeroFila(), caf, nombreBien, e.getMessage()));
        }
    }
}
