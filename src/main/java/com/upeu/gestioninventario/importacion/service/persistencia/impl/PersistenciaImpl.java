package com.upeu.gestioninventario.importacion.service.persistencia.impl;

import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.importacion.dto.*;
import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.HojaAnalizadaDTO;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverResponsableService;
import com.upeu.gestioninventario.importacion.service.persistencia.IPersistenciaPorFormato;
import com.upeu.gestioninventario.importacion.service.persistencia.IPersistencia;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("persistenciaV3")
@Slf4j
public class PersistenciaImpl implements IPersistencia {

    private final List<IPersistenciaPorFormato> estrategias;
    private final IResolverResponsableService resolverResponsableService;

    public PersistenciaImpl(
            List<IPersistenciaPorFormato> estrategias,
            IResolverResponsableService resolverResponsableService) {
        this.estrategias = estrategias;
        this.resolverResponsableService = resolverResponsableService;
    }

    @Override
    @Transactional
    public ImportacionResultadoDTO ejecutar(SesionAnalisisV3DTO sesion) {
        log.info("Iniciando persistencia V3 - Archivo: {}, Hojas: {}",
                sesion.nombreArchivo(),
                sesion.hojas() != null ? sesion.hojas().size() : 0);

        Usuario usuario = resolverResponsableService.resolverUsuario()
                .orElseThrow(() -> new IllegalStateException("No se pudo obtener el usuario actual"));

        Persona responsableDefault = usuario.getPersona();
        Departamento departamento = usuario.getDepartamento();
        Long idUsuario = usuario.getId();

        ResultadoAcumulado acumulado = new ResultadoAcumulado();

        if (sesion.hojas() != null) {
            for (HojaAnalizadaDTO hoja : sesion.hojas()) {
                procesarHoja(hoja, idUsuario, responsableDefault, departamento, acumulado);
            }
        }

        log.info("Persistencia completada - Bienes: {}, Estaciones: {}, Errores: {}",
                acumulado.bienesCreados, acumulado.estacionesCreadas, acumulado.errores.size());

        return construirResultado(acumulado);
    }

    private void procesarHoja(
            HojaAnalizadaDTO hoja,
            Long idUsuario,
            Persona responsableDefault,
            Departamento departamento,
            ResultadoAcumulado acumulado) {

        TipoFormatoDetectado formato = hoja.metadatos().tipoFormato();
        String nombreHoja = hoja.metadatos().nombreHoja();

        IPersistenciaPorFormato estrategia = obtenerEstrategia(formato);

        if (estrategia == null) {
            log.warn("No hay estrategia para formato {} en hoja '{}'. Saltando.", formato, nombreHoja);
            return;
        }

        log.info("Procesando hoja '{}' con estrategia {}", nombreHoja, estrategia.getClass().getSimpleName());

        ResultadoPersistenciaHoja resultado = estrategia.persistir(
                hoja, idUsuario, responsableDefault, departamento
        );

        acumularResultado(resultado, acumulado);
    }

    private IPersistenciaPorFormato obtenerEstrategia(TipoFormatoDetectado formato) {
        return estrategias.stream()
                .filter(e -> e.soporta(formato))
                .findFirst()
                .orElse(null);
    }

    private void acumularResultado(ResultadoPersistenciaHoja resultado, ResultadoAcumulado acumulado) {
        acumulado.bienesCreados += resultado.bienesCreados();
        acumulado.bienesFallidos += resultado.bienesFallidos();
        acumulado.estacionesCreadas += resultado.estacionesCreadas();
        acumulado.estacionesFallidas += resultado.estacionesFallidas();
        acumulado.detalles.addAll(resultado.detalles());
        acumulado.errores.addAll(resultado.errores());
    }

    private ImportacionResultadoDTO construirResultado(ResultadoAcumulado r) {
        List<ImportacionItemResultadoDTO> fallidos = r.detalles.stream()
                .filter(d -> !d.exitoso())
                .toList();

        return new ImportacionResultadoDTO(
                r.bienesCreados,
                0,
                fallidos.size(),
                r.errores.size(),
                generarMensaje(r),
                r.errores,
                r.detalles,
                fallidos,
                r.estacionesCreadas,
                0,
                r.estacionesFallidas,
                r.bienesCreados,
                List.of()
        );
    }

    private String generarMensaje(ResultadoAcumulado r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.bienesCreados).append(" bienes creados");
        if (r.estacionesCreadas > 0) {
            sb.append(", ").append(r.estacionesCreadas).append(" estaciones");
        }
        if (r.bienesFallidos > 0 || r.estacionesFallidas > 0) {
            sb.append(", ").append(r.bienesFallidos + r.estacionesFallidas).append(" fallidos");
        }
        return sb.toString();
    }

    private static class ResultadoAcumulado {
        int bienesCreados = 0;
        int bienesFallidos = 0;
        int estacionesCreadas = 0;
        int estacionesFallidas = 0;
        List<FilaErrorDTO> errores = new ArrayList<>();
        List<ImportacionItemResultadoDTO> detalles = new ArrayList<>();
    }
}
