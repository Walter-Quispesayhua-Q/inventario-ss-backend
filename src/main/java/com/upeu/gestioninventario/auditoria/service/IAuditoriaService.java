package com.upeu.gestioninventario.auditoria.service;

import com.upeu.gestioninventario.auditoria.dto.RegistroActividadCreacionInput;
import com.upeu.gestioninventario.auditoria.dto.RegistroActividadDTO;
import com.upeu.gestioninventario.auditoria.dto.RegistrosActividadPaginados;
import com.upeu.gestioninventario.auditoria.model.TipoEndpoint;
import com.upeu.gestioninventario.auditoria.model.TipoOperacion;
import com.upeu.gestioninventario.auth.model.Usuario;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface IAuditoriaService {

    void registrarActividad(
            TipoOperacion tipoOperacion,
            TipoEndpoint tipoEndpoint,
            String entidadAfectada,
            Long entidadId,
            String descripcion,
            Map<String, Object> metadatos,
            String ipAddress,
            String userAgent,
            Usuario usuario
    );

    OperacionResultadoDTO<RegistroActividadDTO> crearRegistroManual(RegistroActividadCreacionInput input);

    RegistrosActividadPaginados obtenerRegistrosPaginados(
            Integer page,
            Integer limit,
            String buscar,
            Long usuarioId,
            String tipoOperacion,
            String entidadAfectada,
            Long entidadId,
            OffsetDateTime fechaInicio,
            OffsetDateTime fechaFin,
            String ordenarPor,
            String orden
    );

    RegistroActividadDTO obtenerRegistroPorId(Long id);

    List<RegistroActividadDTO> obtenerActividadesPorUsuario(Long usuarioId);

    List<RegistroActividadDTO> obtenerActividadesPorEntidad(String entidadAfectada, Long entidadId);

    Usuario obtenerUsuarioActual();

    Usuario obtenerUsuarioPorId(Long id);
}
