package com.upeu.gestioninventario.importacion.dto.formato.respuesta;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.importacion.dto.EstadisticasImportacionDTO;
import com.upeu.gestioninventario.importacion.dto.ResponsableConEstacionesDTO;

import java.util.List;

public record RespuestaImportacionDTO(
        //INFO GENERAL
        boolean esArchivoValido,
        String nombreArchivo,
        String sesionId,

        //HOJAS ANALIZADAS
        List<HojaAnalizadaDTO> hojas,

        //RESUMEN GLOBAL
        ResumenGlobalDTO resumen,

        //ESTACIONES AGRUPADAS POR RESPONSABLE
        List<ResponsableConEstacionesDTO> estacionesPorResponsable,

        //MENSAJES
        String mensajeResumen,
        List<String> sugerencias,
        List<String> errores
) {

    public record ResumenGlobalDTO(
            int totalHojas,
            int totalFilas,
            int totalEstaciones,
            int totalBienes,
            EstadisticasImportacionDTO estadisticas
    ) {}

    public static RespuestaImportacionDTO archivoInvalido(String nombreArchivo, String error) {
        return new RespuestaImportacionDTO(
                false,
                nombreArchivo,
                null,
                List.of(),
                new ResumenGlobalDTO(0, 0, 0, 0, null),
                List.of(),
                "Archivo inválido: " + error,
                List.of(),
                List.of(error)
        );
    }

    public static RespuestaImportacionDTO exito(
            String nombreArchivo,
            String sesionId,
            List<HojaAnalizadaDTO> hojas,
            List<ResponsableConEstacionesDTO> porResponsable,
            EstadisticasImportacionDTO estadisticas) {

        int totalFilas = hojas.stream()
                .mapToInt(h -> h.metadatos().totalFilas())
                .sum();
        int totalEstaciones = hojas.stream()
                .mapToInt(h -> h.estacionesSugeridas() != null ? h.estacionesSugeridas().size() : 0)
                .sum();
        int totalBienes = hojas.stream()
                .flatMap(h -> h.estacionesSugeridas() != null ? h.estacionesSugeridas().stream() : java.util.stream.Stream.empty())
                .mapToInt(EstacionConComponentesDTO::getTotalBienes)
                .sum();

        return new RespuestaImportacionDTO(
                true,
                nombreArchivo,
                sesionId,
                hojas,
                new ResumenGlobalDTO(hojas.size(), totalFilas, totalEstaciones, totalBienes, estadisticas),
                porResponsable,
                String.format("Archivo procesado: %d hojas, %d filas, %d estaciones",
                        hojas.size(), totalFilas, totalEstaciones),
                List.of(),
                List.of()
        );
    }
}