package com.upeu.gestioninventario.importacion.dto.formato.respuesta;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.importacion.dto.FilaPreviewDTO;

import java.util.List;
import java.util.Map;

public record HojaAnalizadaDTO(
        MetadatosHojaDTO metadatos,
        EstructuraHojaDTO estructura,
        FormatoGridDTO formatoGrid,
        //DATOS PUROS
        DatosHojaDTO datos,

        List<FilaPreviewDTO> filasEnriquecidas,
        List<EstacionConComponentesDTO> estacionesSugeridas,
        List<FichaResumenDTO> fichasDetectadas
) {

    public record FichaResumenDTO(
            String id,
            int filaInicio,
            int filaFin,
            int totalFilas,
            Map<String, String> metadatos,
            String categoriaDetectada
    ) {}

    public static HojaAnalizadaDTO paraMatriz(
            MetadatosHojaDTO metadatos,
            EstructuraHojaDTO estructura,
            FormatoGridDTO formato,
            List<List<String>> datosMatriz,
            List<FilaPreviewDTO> filasEnriquecidas,
            List<EstacionConComponentesDTO> estaciones,
            List<FichaResumenDTO> fichas) {

        return new HojaAnalizadaDTO(
                metadatos, estructura, formato,
                DatosHojaDTO.paraMatriz(datosMatriz),
                filasEnriquecidas, estaciones, fichas
        );
    }

    public static HojaAnalizadaDTO paraTabular(
            MetadatosHojaDTO metadatos,
            EstructuraHojaDTO estructura,
            FormatoGridDTO formato,
            List<Map<String, String>> filas,
            List<FilaPreviewDTO> filasEnriquecidas,
            List<EstacionConComponentesDTO> estaciones) {

        return new HojaAnalizadaDTO(
                metadatos, estructura, formato,
                DatosHojaDTO.paraTabular(filas),
                filasEnriquecidas, estaciones, List.of()
        );
    }

    public static HojaAnalizadaDTO paraClaveValor(
            MetadatosHojaDTO metadatos,
            EstructuraHojaDTO estructura,
            FormatoGridDTO formato,
            List<DatosHojaDTO.ParClaveValorDTO> pares,
            List<FilaPreviewDTO> filasEnriquecidas,
            List<EstacionConComponentesDTO> estaciones,
            List<FichaResumenDTO> fichas) {

        return new HojaAnalizadaDTO(
                metadatos, estructura, formato,
                DatosHojaDTO.paraClaveValor(pares),
                filasEnriquecidas, estaciones, fichas
        );
    }
}