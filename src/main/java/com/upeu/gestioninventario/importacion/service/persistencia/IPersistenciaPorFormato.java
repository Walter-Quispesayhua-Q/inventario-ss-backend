package com.upeu.gestioninventario.importacion.service.persistencia;

import com.upeu.gestioninventario.importacion.dto.ResultadoPersistenciaHoja;
import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.HojaAnalizadaDTO;
import com.upeu.gestioninventario.personas.model.Persona;
import com.upeu.gestioninventario.ubicaciones.model.Departamento;

public interface IPersistenciaPorFormato {

    ResultadoPersistenciaHoja persistir(
            HojaAnalizadaDTO hoja,
            Long idUsuario,
            Persona responsableDefault,
            Departamento departamento
    );

    TipoFormatoDetectado getFormatoSoportado();

    default boolean soporta(TipoFormatoDetectado formato) {
        return getFormatoSoportado() == formato;
    }
}
