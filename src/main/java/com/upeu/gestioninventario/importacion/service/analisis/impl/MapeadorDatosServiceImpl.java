package com.upeu.gestioninventario.importacion.service.analisis.impl;

import com.upeu.gestioninventario.importacion.dto.ColumnaDetectadaDTO;
import com.upeu.gestioninventario.importacion.service.analisis.IMapeadorDatos;
import com.upeu.gestioninventario.ml.service.PlantillaLoaderService;
import com.upeu.gestioninventario.ml.dto.seed.AtributoExtraccionSeedDTO;
import com.upeu.gestioninventario.ml.dto.seed.PlantillaSeedDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapeadorDatosServiceImpl implements IMapeadorDatos {

    private final PlantillaLoaderService plantillaLoaderService;
    private final Map<String, String> mapaDeAliasGlobal = new HashMap<>();

    @PostConstruct
    public void inicializarMapeador() {
        log.info("Inicializando MapeadorDatos");
        Collection<PlantillaSeedDTO> todasLasPlantillas = plantillaLoaderService.getPlantillasCache().values();

        for (PlantillaSeedDTO plantilla : todasLasPlantillas) {
            if (plantilla.atributosExtraccion() == null) continue;

            for (AtributoExtraccionSeedDTO atributo : plantilla.atributosExtraccion()) {
                String nombreCampo = atributo.nombreCampo();
                Map<String, Object> jsonExtraccion = atributo.extraccion();

                if (jsonExtraccion != null && jsonExtraccion.get("estrategias") instanceof List<?> estrategias) {
                    for (Object estrategiaObj : estrategias) {
                        if (estrategiaObj instanceof Map<?, ?> estrategiaMap) {
                            if ("COLUMNA_MAPEADA".equals(estrategiaMap.get("tipo")) &&
                                    estrategiaMap.get("nombresColumna") instanceof List<?> aliasList) {
                                for (Object aliasObj : aliasList) {
                                    if (aliasObj instanceof String nombreAlias) {
                                        mapaDeAliasGlobal.put(normalizar(nombreAlias), nombreCampo);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        log.info("MapeadorDatos inicializado con {} alias", mapaDeAliasGlobal.size());
    }

    @Override
    public List<ColumnaDetectadaDTO> mapearNombres(List<String> nombres) {
        List<ColumnaDetectadaDTO> mapeo = new ArrayList<>();
        for (int i = 0; i < nombres.size(); i++) {
            String nombreNormalizado = normalizar(nombres.get(i));
            String campoMapeado = mapaDeAliasGlobal.get(nombreNormalizado);
            mapeo.add(new ColumnaDetectadaDTO(
                    i,
                    nombres.get(i),
                    campoMapeado,
                    campoMapeado != null ? 1.0 : 0.0,
                    campoMapeado != null
            ));
        }
        log.info("Mapeo finalizado: {} de {} nombres mapeados",
                mapeo.stream().filter(ColumnaDetectadaDTO::mapeada).count(),
                nombres.size());
        return mapeo;
    }

    @Override
    public String mapearClave(String clave) {
        if (clave == null) return null;
        return mapaDeAliasGlobal.get(normalizar(clave));
    }

    @Override
    public boolean esCampoConocido(String texto) {
        if (texto == null) return false;
        return mapaDeAliasGlobal.containsKey(normalizar(texto));
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        return texto.toLowerCase()
                .trim()
                .replaceAll("[_\\-\\s]+", " ");
    }
}
