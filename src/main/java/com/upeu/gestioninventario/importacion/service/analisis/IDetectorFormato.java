package com.upeu.gestioninventario.importacion.service.analisis;


import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface IDetectorFormato {

    record ResultadoDeteccionHoja(
            int indiceHoja,
            String nombreHoja,
            TipoFormatoDetectado formatoDetectado,
            int scoreTabular,
            int scoreClaveValor,
            int scoreMatriz,
            int scoreMaximo
    ) {}

    Map<Integer, ResultadoDeteccionHoja> detectarFormatoTodasHojas(MultipartFile archivo) throws IOException;


    ResultadoDeteccionHoja analizarHoja(Sheet hoja, int indice);
}
