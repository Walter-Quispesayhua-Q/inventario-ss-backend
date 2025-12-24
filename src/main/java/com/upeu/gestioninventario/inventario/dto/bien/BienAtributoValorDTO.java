package com.upeu.gestioninventario.inventario.dto.bien;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record BienAtributoValorDTO(
        Long id,

        @JsonProperty("bienId")
        Long bienId,

        @JsonProperty("tipoAtributoId")
        Long tipoAtributoId,

        String nombreAtributo,
        String tipoDato,
        String etiqueta,

        String valor,

        LocalDateTime fechaCreacion,
        LocalDateTime fechaUltimaModificacion
) {

    public static BienAtributoValorDTO nuevo(
            Long bienId,
            Long tipoAtributoId,
            String valor
    ) {
        return new BienAtributoValorDTO(
                null, bienId, tipoAtributoId, null, null, null,
                valor, null, null
        );
    }
}