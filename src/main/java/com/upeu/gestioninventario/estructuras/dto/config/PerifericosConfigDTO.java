package com.upeu.gestioninventario.estructuras.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerifericosConfigDTO {

    private String descripcion;
    private List<String> categorias;
    private ConfiguracionPerifericosDTO configuracion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConfiguracionPerifericosDTO {
        private Boolean agruparPorCategoria;
        private String tipo;
    }

    public boolean esPeriferico(String categoria) {
        if (categorias == null || categoria == null) return false;
        String catNormalizada = categoria.toLowerCase().trim();
        return categorias.stream()
                .anyMatch(cat -> catNormalizada.contains(cat.toLowerCase()));
    }
}