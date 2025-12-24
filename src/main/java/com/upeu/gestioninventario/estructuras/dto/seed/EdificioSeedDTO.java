package com.upeu.gestioninventario.estructuras.dto.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EdificioSeedDTO(
        EdificioData edificio
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EdificioData(
            String nombre,
            String codigo,
            String tipoEstructura,
            String direccion,
            Integer numeroPisos,
            BigDecimal areaTotalM2,
            Integer anoConstruccion,
            Long idResponsableMantenimiento,
            String observaciones,
            List<PisoData> pisos
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PisoData(
            String nombre,
            String codigo,
            Integer numeroPiso,
            String tipoEstructura,
            Long idResponsableMantenimiento,
            String observaciones,
            List<AmbienteData> ambientes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AmbienteData(
            String nombre,
            String codigo,
            String tipoEstructura,
            Integer capacidadPersonas,
            Long idResponsable,
            String observaciones,
            List<String> palabrasClaveUbicacion
    ) {}
}
