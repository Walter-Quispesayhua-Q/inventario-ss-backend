package com.upeu.gestioninventario.estructuras.dto.estacion;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BienEstacionPaginadoDTO {
    
    private List<BienEstacionDTO> contenido;
    private Integer totalElementos;
    private Integer totalPaginas;
    private Integer paginaActual;
    private Integer tamanoPagina;
    private Boolean primeraPagina;
    private Boolean ultimaPagina;
}
