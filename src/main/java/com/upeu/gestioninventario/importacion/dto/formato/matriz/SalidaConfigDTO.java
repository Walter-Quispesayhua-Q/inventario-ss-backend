package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SalidaConfigDTO(
        Boolean agruparPorFicha,
        Boolean incluirMetadatosFicha,
        Boolean incluirCodigoFicha,
        FormatoEstacionDTO formatoEstacion
) {
    public SalidaConfigDTO {
        if (agruparPorFicha == null) agruparPorFicha = true;
        if (incluirMetadatosFicha == null) incluirMetadatosFicha = true;
        if (incluirCodigoFicha == null) incluirCodigoFicha = true;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FormatoEstacionDTO(
            Boolean generarCodigoAutomatico,
            String prefijoEstacion,
            Boolean usarCodigoFichaComoNombre
    ) {
        public FormatoEstacionDTO {
            if (generarCodigoAutomatico == null) generarCodigoAutomatico = true;
            if (prefijoEstacion == null) prefijoEstacion = "EST";
            if (usarCodigoFichaComoNombre == null) usarCodigoFichaComoNombre = true;
        }
    }
}