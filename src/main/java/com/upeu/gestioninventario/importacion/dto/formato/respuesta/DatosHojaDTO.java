package com.upeu.gestioninventario.importacion.dto.formato.respuesta;

import java.util.List;
import java.util.Map;

public record DatosHojaDTO(
        List<List<String>> datosMatriz,
        List<Map<String, String>> filasTabular,
        List<ParClaveValorDTO> paresClaveValor
) {

    public record ParClaveValorDTO(
            String clave,
            String valor,
            String claveOriginal,
            boolean claveReconocida
    ) {}

    public static DatosHojaDTO paraMatriz(List<List<String>> datos) {
        return new DatosHojaDTO(datos, List.of(), List.of());
    }

    public static DatosHojaDTO paraTabular(List<Map<String, String>> filas) {
        return new DatosHojaDTO(List.of(), filas, List.of());
    }

    public static DatosHojaDTO paraClaveValor(List<ParClaveValorDTO> pares) {
        return new DatosHojaDTO(List.of(), List.of(), pares);
    }
}