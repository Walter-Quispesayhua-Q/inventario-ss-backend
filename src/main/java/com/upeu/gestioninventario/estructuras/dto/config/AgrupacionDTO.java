package com.upeu.gestioninventario.estructuras.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgrupacionDTO {

    private String id;
    private String nombreComponente;
    private String tipo;
    private String categoriaBase;
    private String descripcion;
    private Integer orden;
    private List<String> componentesQueAgrupa;
    private List<String> palabrasClaveDeteccion;
    private ConfiguracionAgrupacionDTO configuracion;

    public boolean contieneComponente(String nombreBien) {
        if (componentesQueAgrupa == null || nombreBien == null) return false;
        String nombreNormalizado = nombreBien.toLowerCase().trim();
        return componentesQueAgrupa.stream()
                .anyMatch(comp -> nombreNormalizado.contains(comp.toLowerCase()));
    }

    public boolean coincideConPalabraClave(String texto) {
        if (palabrasClaveDeteccion == null || texto == null) return false;
        String textoNormalizado = texto.toLowerCase().trim();
        return palabrasClaveDeteccion.stream()
                .anyMatch(palabra -> textoNormalizado.contains(palabra.toLowerCase()));
    }
}