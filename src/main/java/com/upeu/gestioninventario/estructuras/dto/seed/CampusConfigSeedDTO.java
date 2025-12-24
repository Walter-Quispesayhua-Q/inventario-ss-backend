package com.upeu.gestioninventario.estructuras.dto.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CampusConfigSeedDTO(
        CampusData campus,
        ConfiguracionData configuracion
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CampusData(
            String codigo,
            String universidad,
            String nombre,
            String ubicacion,
            Integer anioActualizacion,
            ContactoData contacto
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContactoData(
            String telefono,
            String email
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConfiguracionData(
            Boolean autoDescubrir,
            String carpetaEdificios,
            String extensionArchivos
    ) {}
}
