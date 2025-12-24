package com.upeu.gestioninventario.importacion.service.analisis.lectores;

import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.importacion.dto.FilaPreviewDTO;
import com.upeu.gestioninventario.importacion.dto.ResultadoDeteccionCategoria;
import com.upeu.gestioninventario.importacion.dto.formato.TipoFormatoDetectado;
import com.upeu.gestioninventario.importacion.dto.formato.clavevalor.FormatoClaveValorDTO;
import com.upeu.gestioninventario.importacion.dto.formato.respuesta.*;
import com.upeu.gestioninventario.importacion.service.analisis.IBuscadorAmbiente;
import com.upeu.gestioninventario.importacion.service.analisis.IDetectorCategoria;
import com.upeu.gestioninventario.importacion.service.analisis.IExtractorAtributos;
import com.upeu.gestioninventario.importacion.service.analisis.IMapeadorDatos;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.builder.EstacionBuilder;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.util.ExcelCellReader;
import com.upeu.gestioninventario.importacion.service.analisis.lectores.util.HeaderDetector;
import com.upeu.gestioninventario.ml.service.PlantillaLoaderService;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class LectorClaveValorService extends AbstractLectorFormato implements ILectorFormato<FormatoClaveValorDTO> {

    private final PlantillaLoaderService plantillaLoaderService;

    public LectorClaveValorService(
            IMapeadorDatos mapeadorDatos,
            IDetectorCategoria detectorCategoria,
            IExtractorAtributos extractorAtributos,
            IBuscadorAmbiente buscadorAmbiente,
            ExcelCellReader cellReader,
            HeaderDetector headerDetector,
            EstacionBuilder estacionBuilder,
            PlantillaLoaderService plantillaLoaderService) {
        super(mapeadorDatos, detectorCategoria, extractorAtributos, buscadorAmbiente, cellReader, headerDetector, estacionBuilder);
        this.plantillaLoaderService = plantillaLoaderService;
    }

    @Override
    public HojaAnalizadaDTO extraerDatos(Sheet hoja, FormatoClaveValorDTO config, int indiceHoja) {
        log.info("Extrayendo datos CLAVE_VALOR de hoja: {}", hoja.getSheetName());

        int colClave = config.claves().columna() != null ? convertirLetraAColumna(config.claves().columna()) : 0;
        int colValor = config.valores().columna() != null ? convertirLetraAColumna(config.valores().columna()) : 1;

        List<DatosHojaDTO.ParClaveValorDTO> paresClaveValor = new ArrayList<>();
        List<FilaPreviewDTO> filasEnriquecidas = new ArrayList<>();
        List<HojaAnalizadaDTO.FichaResumenDTO> fichasDetectadas = new ArrayList<>();

        Map<String, String> fichaActual = new LinkedHashMap<>();
        int filaInicioFicha = -1;
        String idFichaActual = null;

        for (int i = 0; i <= hoja.getLastRowNum(); i++) {
            Row fila = hoja.getRow(i);

            if (fila == null || esFilaVacia(fila)) {
                if (!fichaActual.isEmpty()) {
                    fichasDetectadas.add(crearFichaResumen(idFichaActual, filaInicioFicha, i - 1, fichaActual));
                    fichaActual = new LinkedHashMap<>();
                    filaInicioFicha = -1;
                    idFichaActual = null;
                }
                continue;
            }

            String clave = leerValorCelda(fila.getCell(colClave));
            String valor = leerValorCelda(fila.getCell(colValor));

            if (esPatronNuevaFicha(clave, config)) {
                if (!fichaActual.isEmpty()) {
                    fichasDetectadas.add(crearFichaResumen(idFichaActual, filaInicioFicha, i - 1, fichaActual));
                }
                fichaActual = new LinkedHashMap<>();
                filaInicioFicha = i;
                idFichaActual = clave;
            }

            if (filaInicioFicha == -1) filaInicioFicha = i;

            String campoMapeado = mapeadorDatos.mapearClave(clave);
            boolean claveReconocida = campoMapeado != null;

            if (!clave.isEmpty()) {
                fichaActual.put(claveReconocida ? campoMapeado : clave, valor);

                paresClaveValor.add(new DatosHojaDTO.ParClaveValorDTO(
                        claveReconocida ? campoMapeado : clave,
                        valor,
                        clave,
                        claveReconocida
                ));
            }
        }

        if (!fichaActual.isEmpty()) {
            fichasDetectadas.add(crearFichaResumen(idFichaActual, filaInicioFicha, hoja.getLastRowNum(), fichaActual));
        }

        for (HojaAnalizadaDTO.FichaResumenDTO ficha : fichasDetectadas) {
            String textoCompleto = String.join(" ", ficha.metadatos().values());
            ResultadoDeteccionCategoria categoria = detectorCategoria.detectarCategoria(textoCompleto);

            Map<String, String> atributosExtraidos = new HashMap<>();
            String categoriaDetectada = "Desconocida";
            double confianza = 0.0;

            if (categoria != null && categoria.plantilla() != null) {
                categoriaDetectada = categoria.plantilla().nombrePlantilla();
                confianza = categoria.confianza();

                PlantillaSeedDTO plantilla = plantillaLoaderService.getPlantillasCache()
                        .get(categoria.plantilla().idPlantillaSeed());

                if (plantilla != null) {
                    atributosExtraidos = extractorAtributos.extraerAtributos(textoCompleto, ficha.metadatos(), plantilla);
                }
            }

            filasEnriquecidas.add(construirFilaPreview(
                    ficha.filaInicio() + 1, ficha.metadatos(), atributosExtraidos, categoriaDetectada, confianza
            ));
        }

        List<EstacionConComponentesDTO> estacionesSugeridas = new ArrayList<>();

        if (config.fichasMultiples() != null && config.fichasMultiples().habilitadas()) {
            Map<String, List<FilaPreviewDTO>> gruposPorFicha = agruparPorFichas(filasEnriquecidas, config.fichasMultiples());

            int indiceEstacion = 1;
            for (Map.Entry<String, List<FilaPreviewDTO>> entry : gruposPorFicha.entrySet()) {
                AmbienteDetectadoDTO ambiente = entry.getValue().isEmpty()
                        ? AmbienteDetectadoDTO.noDetectado()
                        : entry.getValue().get(0).ambienteDetectado();

                EstacionConComponentesDTO estacion = crearEstacionDesdeGrupo(
                        entry.getKey(), entry.getValue(), ambiente, indiceEstacion++);

                if (estacion != null) {
                    String responsable = entry.getValue().stream()
                            .map(f -> extraerResponsableDeDatos(f.valoresOriginales()))
                            .filter(r -> r != null && !r.isBlank())
                            .findFirst()
                            .orElse(null);
                    estacion.setResponsableDetectado(responsable);
                    estacionesSugeridas.add(estacion);
                }
            }

            log.info("Se detectaron {} estacion(es) en formato CLAVE_VALOR", estacionesSugeridas.size());
        }

        MetadatosHojaDTO metadatos = construirMetadatosHoja(
                TipoFormatoDetectado.CLAVE_VALOR,
                hoja.getSheetName(),
                indiceHoja,
                paresClaveValor.size(),
                2,
                85
        );

        EstructuraHojaDTO estructura = EstructuraHojaDTO.soloColumnas(List.of("Clave", "Valor"));

        FormatoGridDTO formatoGrid = FormatoGridDTO.crear(
                obtenerCoordenada(0, colClave),
                obtenerCoordenada(hoja.getLastRowNum(), colValor),
                -1,
                colClave
        );

        return HojaAnalizadaDTO.paraClaveValor(
                metadatos,
                estructura,
                formatoGrid,
                paresClaveValor,
                filasEnriquecidas,
                estacionesSugeridas,
                fichasDetectadas
        );
    }

    private boolean esPatronNuevaFicha(String texto, FormatoClaveValorDTO config) {
        if (texto == null || texto.isEmpty()) return false;
        if (config.fichasMultiples() == null || !config.fichasMultiples().habilitadas()) return false;

        var separadores = config.fichasMultiples().separadores();
        if (separadores != null && separadores.porPatron() != null && separadores.porPatron().habilitado()) {
            for (String patron : separadores.porPatron().patrones()) {
                if (texto.matches(patron)) return true;
            }
        }
        return false;
    }

    private HojaAnalizadaDTO.FichaResumenDTO crearFichaResumen(
            String id, int filaInicio, int filaFin, Map<String, String> atributos) {
        return new HojaAnalizadaDTO.FichaResumenDTO(
                id != null ? id : "FICHA-" + (filaInicio + 1),
                filaInicio,
                filaFin,
                filaFin - filaInicio + 1,
                new HashMap<>(atributos),
                null
        );
    }

    @Override
    public boolean soportaFormato(FormatoClaveValorDTO config) {
        return config != null && "CLAVE_VALOR".equals(config.tipoFormato());
    }
}