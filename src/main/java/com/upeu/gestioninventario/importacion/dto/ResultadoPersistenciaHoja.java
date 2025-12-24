package com.upeu.gestioninventario.importacion.dto;

import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;

import java.util.ArrayList;
import java.util.List;

public record ResultadoPersistenciaHoja(
        int indiceHoja,
        String nombreHoja,
        TipoFormatoDetectado formato,
        int bienesCreados,
        int bienesFallidos,
        int estacionesCreadas,
        int estacionesFallidas,
        List<ImportacionItemResultadoDTO> detalles,
        List<FilaErrorDTO> errores
) {

    public static ResultadoPersistenciaHoja vacio(int indice, String nombre, TipoFormatoDetectado formato) {
        return new ResultadoPersistenciaHoja(
                indice, nombre, formato,
                0, 0, 0, 0,
                List.of(), List.of()
        );
    }

    public static Builder builder(int indice, String nombre, TipoFormatoDetectado formato) {
        return new Builder(indice, nombre, formato);
    }

    public static class Builder {
        private final int indiceHoja;
        private final String nombreHoja;
        private final TipoFormatoDetectado formato;
        private int bienesCreados = 0;
        private int bienesFallidos = 0;
        private int estacionesCreadas = 0;
        private int estacionesFallidas = 0;
        private final List<ImportacionItemResultadoDTO> detalles = new ArrayList<>();
        private final List<FilaErrorDTO> errores = new ArrayList<>();

        private Builder(int indice, String nombre, TipoFormatoDetectado formato) {
            this.indiceHoja = indice;
            this.nombreHoja = nombre;
            this.formato = formato;
        }

        public Builder bienCreado(ImportacionItemResultadoDTO detalle) {
            bienesCreados++;
            detalles.add(detalle);
            return this;
        }

        public Builder bienFallido(FilaErrorDTO error) {
            bienesFallidos++;
            errores.add(error);
            return this;
        }

        public Builder estacionCreada() {
            estacionesCreadas++;
            return this;
        }

        public Builder estacionFallida(FilaErrorDTO error) {
            estacionesFallidas++;
            errores.add(error);
            return this;
        }

        public ResultadoPersistenciaHoja build() {
            return new ResultadoPersistenciaHoja(
                    indiceHoja, nombreHoja, formato,
                    bienesCreados, bienesFallidos,
                    estacionesCreadas, estacionesFallidas,
                    List.copyOf(detalles), List.copyOf(errores)
            );
        }
    }
}
