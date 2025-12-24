package com.upeu.gestioninventario.importacion.service.persistencia.util;

import com.upeu.gestioninventario.categorias.model.TipoAtributo;
import com.upeu.gestioninventario.categorias.repository.TipoAtributoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ResolverIdTipoAtributo {

    private final TipoAtributoRepository tipoAtributoRepository;
    private final Map<String, Long> cacheNombreAId = new ConcurrentHashMap<>();
    private final Map<String, String[]> aliasCanonicos = new ConcurrentHashMap<>();


    @PostConstruct
    public void inicializarCache() {
        log.info("Inicializando caché de IDs de Tipos de Atributo...");
        try {
            Map<String, Long> mapaInicial = tipoAtributoRepository.findAll().stream()
                    .collect(Collectors.toMap(
                            atributo -> atributo.getNombreAtributo().toUpperCase(Locale.ROOT),
                            TipoAtributo::getId,
                            (idExistente, idNuevo) -> idExistente
                    ));
            cacheNombreAId.putAll(mapaInicial);
            aliasCanonicos.put("CAF", new String[]{"CAF", "CODIGO CAF", "CÓDIGO CAF", "CODIGO", "CODE", "TAG", "ACTIVO", "ACTIVO FIJO", "INVENTARIO", "INV"});
            aliasCanonicos.put("NUMERO_SERIE", new String[]{"NUMERO SERIE", "NÚMERO SERIE", "SERIE", "SERIAL", "S/N", "SN", "SERIAL NUMBER"});
            aliasCanonicos.put("OBSERVACIONES", new String[]{"OBSERVACION", "OBSERVACIONES", "COMENTARIO", "COMENTARIOS", "NOTA", "NOTAS", "REMARKS"});
        } catch (Exception e) {
            log.error("CRÍTICO: Falló la inicialización de la caché de Tipos de Atributo. El servicio de estandarización de nombres podría no funcionar.", e);
        }
        log.info("Caché de IDs de Tipos de Atributo inicializada con {} entradas.", cacheNombreAId.size());
    }

}