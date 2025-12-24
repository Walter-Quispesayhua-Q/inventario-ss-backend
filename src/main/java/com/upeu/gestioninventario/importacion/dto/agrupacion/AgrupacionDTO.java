package com.upeu.gestioninventario.importacion.dto.agrupacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgrupacionDTO {
    private String id;
    private String nombreComponente;
    private String tipo;
    private String categoriaBase;
    private String descripcion;
    private int orden;
    private List<String> componentesQueAgrupa;
    private List<String> palabrasClaveDeteccion;
    private ConfiguracionAgrupacionDetalleDTO configuracion;
    
    public boolean contieneComponente(String nombreBien) {
        if (nombreBien == null || componentesQueAgrupa == null) {
            return false;
        }
        String nombreNormalizado = nombreBien.toLowerCase().trim();
        return componentesQueAgrupa.stream()
                .anyMatch(comp -> nombreNormalizado.contains(comp.toLowerCase()));
    }

}
