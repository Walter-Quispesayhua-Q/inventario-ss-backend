package com.upeu.gestioninventario.importacion.dto.formato.matriz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EstructuraFichaDTO(
        TituloDTO titulo,
        MetadatosDTO metadatos,
        TablaComponentesDTO tablaComponentes
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TituloDTO(
            String ubicacion,
            Boolean esOpcional,
            String extraerComo,
            String columnaEsperada,
            Boolean puedeSerCeldaCombinada
    ) {
        public TituloDTO {
            if (ubicacion == null) ubicacion = "PRIMERA_FILA_FICHA";
            if (esOpcional == null) esOpcional = true;
            if (extraerComo == null) extraerComo = "CODIGO_FICHA";
            if (columnaEsperada == null) columnaEsperada = "A";
            if (puedeSerCeldaCombinada == null) puedeSerCeldaCombinada = true;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetadatosDTO(
            Boolean habilitado,
            String ubicacion,
            String formato,
            List<String> columnasClave,
            List<String> columnasValor,
            String separadorClaveValor,
            List<CampoEsperadoDTO> camposEsperados,
            FinalizaMetadatosDTO finalizaCuando
    ) {
        public MetadatosDTO {
            if (habilitado == null) habilitado = true;
            if (ubicacion == null) ubicacion = "DESPUES_DE_TITULO";
            if (formato == null) formato = "CLAVE_VALOR";
            if (columnasClave == null) columnasClave = List.of("A");
            if (columnasValor == null) columnasValor = List.of("B", "C", "D");
            if (separadorClaveValor == null) separadorClaveValor = ":";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CampoEsperadoDTO(
            String clave,
            List<String> alias,
            Boolean requerido,
            String extraerPara
    ) {
        public CampoEsperadoDTO {
            if (requerido == null) requerido = false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinalizaMetadatosDTO(
            Boolean detectarHeadersTabla,
            Integer maximoFilasMetadatos,
            Boolean filaVacia
    ) {
        public FinalizaMetadatosDTO {
            if (detectarHeadersTabla == null) detectarHeadersTabla = true;
            if (maximoFilasMetadatos == null) maximoFilasMetadatos = 10;
            if (filaVacia == null) filaVacia = false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TablaComponentesDTO(
            Boolean habilitada,
            String ubicacion,
            DeteccionHeadersDTO deteccionHeaders,
            ColumnasDTO columnas,
            FinalizaTablaDTO finalizaCuando
    ) {
        public TablaComponentesDTO {
            if (habilitada == null) habilitada = true;
            if (ubicacion == null) ubicacion = "DESPUES_DE_METADATOS";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeteccionHeadersDTO(
            String metodo,
            List<String> palabrasClave,
            Integer minimoCoincidencias,
            Integer buscarEnFilas
    ) {
        public DeteccionHeadersDTO {
            if (metodo == null) metodo = "BUSCAR_PALABRAS_CLAVE";
            if (minimoCoincidencias == null) minimoCoincidencias = 2;
            if (buscarEnFilas == null) buscarEnFilas = 5;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ColumnasDTO(
            Boolean deteccionAutomatica,
            Integer minimoColumnas,
            Integer maximoColumnas
    ) {
        public ColumnasDTO {
            if (deteccionAutomatica == null) deteccionAutomatica = true;
            if (minimoColumnas == null) minimoColumnas = 2;
            if (maximoColumnas == null) maximoColumnas = 20;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinalizaTablaDTO(
            Integer filasVaciasConsecutivas,
            Boolean nuevaFichaDetectada,
            Boolean finDeHoja
    ) {
        public FinalizaTablaDTO {
            if (filasVaciasConsecutivas == null) filasVaciasConsecutivas = 1;
            if (nuevaFichaDetectada == null) nuevaFichaDetectada = true;
            if (finDeHoja == null) finDeHoja = true;
        }
    }
}