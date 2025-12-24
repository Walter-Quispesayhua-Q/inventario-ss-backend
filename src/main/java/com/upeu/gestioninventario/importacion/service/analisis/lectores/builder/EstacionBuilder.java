package com.upeu.gestioninventario.importacion.service.analisis.lectores.builder;

import com.upeu.gestioninventario.estructuras.dto.estacion.BienEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.ComponenteEstacionDTO;
import com.upeu.gestioninventario.estructuras.dto.estacion.EstacionConComponentesDTO;
import com.upeu.gestioninventario.estructuras.dto.tipo.TipoComponenteEnum;
import com.upeu.gestioninventario.estructuras.repository.EstacionRepository;
import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.importacion.dto.FilaPreviewDTO;
import com.upeu.gestioninventario.importacion.dto.agrupacion.AgrupacionDTO;
import com.upeu.gestioninventario.importacion.dto.formato.common.FichasMultiplesDTO;
import com.upeu.gestioninventario.importacion.service.analisis.util.AgrupacionComponentesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EstacionBuilder {

    private final AgrupacionComponentesService agrupacionService;
    private final EstacionRepository estacionRepository;

    public TipoComponenteEnum clasificarComponentePorCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return TipoComponenteEnum.PERIFERICO;
        }

        if (agrupacionService != null) {
            String tipo = agrupacionService.getTipoComponente(categoria);
            if ("EQUIPO_COMPLETO".equals(tipo)) {
                return TipoComponenteEnum.EQUIPO_COMPLETO;
            }
            if (agrupacionService.esPeriferico(categoria)) {
                return TipoComponenteEnum.PERIFERICO;
            }
        }

        String categoriaNormalizada = categoria.toLowerCase().trim();
        if (categoriaNormalizada.contains("cpu") || categoriaNormalizada.contains("computador") ||
            categoriaNormalizada.contains("pc") || categoriaNormalizada.contains("laptop") ||
            categoriaNormalizada.contains("mainboard") || categoriaNormalizada.contains("servidor")) {
            return TipoComponenteEnum.EQUIPO_COMPLETO;
        }

        return TipoComponenteEnum.PERIFERICO;
    }

    public EstacionConComponentesDTO crearEstacionDesdeGrupo(
            String idFicha,
            List<FilaPreviewDTO> bienesGrupo,
            AmbienteDetectadoDTO ambiente,
            int indice) {

        if (bienesGrupo == null || bienesGrupo.isEmpty()) {
            return null;
        }

        Map<String, List<FilaPreviewDTO>> bienesPorGrupo = agruparBienesPorGrupoEstacion(bienesGrupo);
        Map<TipoComponenteEnum, List<BienEstacionDTO>> bienesPorTipo = procesarGrupos(bienesPorGrupo);
        List<ComponenteEstacionDTO> componentes = construirComponentes(bienesPorTipo);

        Long idAmbiente = ambiente.tieneAmbiente() ? ambiente.idAmbiente() : null;
        String nombreEstacion = generarNombreEstacion(componentes, indice, idAmbiente);
        String nombreAmbienteFinal = ambiente.tieneAmbiente() 
                ? ambiente.nombreAmbiente() 
                : ambiente.textoOriginalUbicacion();

        return EstacionConComponentesDTO.builder()
                .codigo(nombreEstacion)
                .nombre(nombreEstacion)
                .componentes(componentes)
                .totalComponentes(componentes.size())
                .totalBienes(bienesGrupo.size())
                .idAmbiente(ambiente.tieneAmbiente() ? ambiente.idAmbiente() : null)
                .nombreAmbiente(nombreAmbienteFinal)
                .nombrePiso(ambiente.tieneAmbiente() ? ambiente.nombrePiso() : null)
                .nombreEdificio(ambiente.tieneAmbiente() ? ambiente.nombreEdificio() : null)
                .build();
    }

    private Map<String, List<FilaPreviewDTO>> agruparBienesPorGrupoEstacion(List<FilaPreviewDTO> bienesGrupo) {
        Map<String, List<FilaPreviewDTO>> bienesPorGrupo = new LinkedHashMap<>();
        
        for (FilaPreviewDTO fila : bienesGrupo) {
            String grupoEstacion = fila.atributosExtraidos() != null 
                    ? fila.atributosExtraidos().getOrDefault("GRUPO_ESTACION", "0")
                    : "0";
            bienesPorGrupo.computeIfAbsent(grupoEstacion, k -> new ArrayList<>()).add(fila);
        }

        if (bienesPorGrupo.size() == 1 && bienesPorGrupo.containsKey("0")) {
            return agruparInteligentemente(bienesGrupo);
        }
        
        return bienesPorGrupo;
    }

    private Map<TipoComponenteEnum, List<BienEstacionDTO>> procesarGrupos(
            Map<String, List<FilaPreviewDTO>> bienesPorGrupo) {
        
        Map<TipoComponenteEnum, List<BienEstacionDTO>> bienesPorTipo = new HashMap<>();

        for (Map.Entry<String, List<FilaPreviewDTO>> grupoEntry : bienesPorGrupo.entrySet()) {
            String grupoNumero = grupoEntry.getKey();
            List<FilaPreviewDTO> filasDelGrupo = grupoEntry.getValue();
            
            TipoComponenteEnum tipoGrupo = determinarTipoGrupo(filasDelGrupo);
            String nombreGrupoDetectado = detectarNombreGrupo(filasDelGrupo);
            
            for (FilaPreviewDTO fila : filasDelGrupo) {
                BienEstacionDTO bienDTO = crearBienDTO(fila, grupoNumero, nombreGrupoDetectado);
                bienesPorTipo.computeIfAbsent(tipoGrupo, k -> new ArrayList<>()).add(bienDTO);
            }
        }
        
        return bienesPorTipo;
    }

    private BienEstacionDTO crearBienDTO(FilaPreviewDTO fila, String grupoNumero, String nombreGrupo) {
        Map<String, String> valores = fila.valoresOriginales();
        Map<String, String> atributos = new HashMap<>(fila.atributosExtraidos() != null 
                ? fila.atributosExtraidos() 
                : Map.of());
        
        String nombreBien = obtenerValor(valores, atributos, "BIEN", "EQUIPO", "NOMBRE_BIEN");
        String marca = valores.getOrDefault("MARCA", atributos.getOrDefault("MARCA", null));
        String modelo = valores.getOrDefault("MODELO", atributos.getOrDefault("MODELO", null));
        String caf = obtenerValor(valores, atributos, "CAF", "CÓDIGO");
        String serie = obtenerSerie(valores, atributos);

        copiarAtributosExtra(valores, atributos, marca, modelo);

        return BienEstacionDTO.builder()
                .nombreBien(nombreBien.isEmpty() ? "Sin nombre" : nombreBien)
                .codigoBien(caf.isEmpty() ? "-" : caf)
                .numeroSerieBien(serie)
                .posicionRelativa(grupoNumero + "-" + nombreBien)
                .asignacionActiva(true)
                .observaciones(construirObservaciones(marca, modelo))
                .categoriaDetectada(fila.categoriaDetectada())
                .idGrupo(grupoNumero)
                .nombreGrupo(nombreGrupo)
                .atributosExtraidos(atributos)
                .build();
    }

    private String obtenerValor(Map<String, String> valores, Map<String, String> atributos, String... claves) {
        for (String clave : claves) {
            String valor = valores.get(clave);
            if (valor != null && !valor.isBlank()) return valor;
            valor = atributos.get(clave);
            if (valor != null && !valor.isBlank()) return valor;
        }
        return "";
    }

    private String obtenerSerie(Map<String, String> valores, Map<String, String> atributos) {
        String[] claves = {"SERVICE TAG / SERIE", "SERVICE TAG", "SERIE", "NUMERO_SERIE"};
        for (String clave : claves) {
            String valor = valores.get(clave);
            if (valor != null && !valor.isBlank()) return valor;
        }
        return atributos.getOrDefault("NUMERO_SERIE", atributos.getOrDefault("SERIE", null));
    }

    private void copiarAtributosExtra(Map<String, String> valores, Map<String, String> atributos, 
                                       String marca, String modelo) {
        if (marca != null && !atributos.containsKey("MARCA")) atributos.put("MARCA", marca);
        if (modelo != null && !atributos.containsKey("MODELO")) atributos.put("MODELO", modelo);
        
        String[] columnasExtra = {"TIPO", "CAP./VEL", "CAPACIDAD", "VELOCIDAD", "COLOR", "DIMENSIONES", "PESO"};
        for (String col : columnasExtra) {
            String val = valores.get(col);
            if (val != null && !val.isBlank() && !atributos.containsKey(col)) {
                atributos.put(col, val);
            }
        }
    }

    private String construirObservaciones(String marca, String modelo) {
        StringBuilder sb = new StringBuilder();
        if (marca != null && !marca.isBlank()) sb.append(marca);
        if (modelo != null && !modelo.isBlank()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(modelo);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private List<ComponenteEstacionDTO> construirComponentes(Map<TipoComponenteEnum, List<BienEstacionDTO>> bienesPorTipo) {
        List<ComponenteEstacionDTO> componentes = new ArrayList<>();
        int orden = 1;

        if (bienesPorTipo.containsKey(TipoComponenteEnum.EQUIPO_COMPLETO)) {
            componentes.add(ComponenteEstacionDTO.builder()
                    .nombre("PC Desktop")
                    .tipo("EQUIPO_COMPLETO")
                    .categoriaBase("PC_DESKTOP")
                    .orden(orden++)
                    .bienes(bienesPorTipo.get(TipoComponenteEnum.EQUIPO_COMPLETO))
                    .descripcion("Componentes internos de PC")
                    .build());
        }

        if (bienesPorTipo.containsKey(TipoComponenteEnum.PERIFERICO)) {
            componentes.add(ComponenteEstacionDTO.builder()
                    .nombre("Periféricos")
                    .tipo("PERIFERICO")
                    .categoriaBase("PERIFERICOS")
                    .orden(orden++)
                    .bienes(bienesPorTipo.get(TipoComponenteEnum.PERIFERICO))
                    .descripcion("Periféricos asociados")
                    .build());
        }

        if (bienesPorTipo.containsKey(TipoComponenteEnum.COMPONENTE_SUELTO)) {
            for (BienEstacionDTO bien : bienesPorTipo.get(TipoComponenteEnum.COMPONENTE_SUELTO)) {
                componentes.add(ComponenteEstacionDTO.builder()
                        .nombre(bien.getNombreBien())
                        .tipo("COMPONENTE_SUELTO")
                        .categoriaBase("SIN_CLASIFICAR")
                        .orden(orden++)
                        .bienes(List.of(bien))
                        .descripcion("Componente sin agrupar")
                        .build());
            }
        }

        return componentes;
    }

    public String generarNombreEstacion(List<ComponenteEstacionDTO> componentes, int indiceRelativo, Long idAmbiente) {
        String prefijo = determinarPrefijo(componentes);
        int numeroBase = obtenerSiguienteNumeroDisponible(idAmbiente, prefijo);
        int numeroFinal = numeroBase + indiceRelativo - 1;
        
        log.debug("Generando código: prefijo={}, base={}, relativo={}, final={}", 
                  prefijo, numeroBase, indiceRelativo, numeroFinal);
        
        return prefijo + "-" + String.format("%02d", numeroFinal);
    }

    private String determinarPrefijo(List<ComponenteEstacionDTO> componentes) {
        boolean tienePC = componentes.stream().anyMatch(c -> "EQUIPO_COMPLETO".equals(c.getTipo()));
        if (tienePC) return "PC";

        boolean soloPerifericos = componentes.stream().allMatch(c -> "PERIFERICO".equals(c.getTipo()));
        return soloPerifericos ? "Periféricos" : "Estación";
    }

    public int obtenerSiguienteNumeroDisponible(Long idAmbiente, String prefijo) {
        if (idAmbiente == null) {
            return 1;
        }
        
        return estacionRepository.findMaxNumeroEstacionPorAmbienteYPrefijo(idAmbiente, prefijo)
                .map(max -> max + 1)
                .orElse(1);
    }

    private Map<String, List<FilaPreviewDTO>> agruparInteligentemente(List<FilaPreviewDTO> bienes) {
        Map<String, List<FilaPreviewDTO>> resultado = new LinkedHashMap<>();
        
        if (agrupacionService == null) {
            resultado.put("0", bienes);
            return resultado;
        }
        
        List<String> nombresBienes = bienes.stream()
                .map(f -> obtenerValor(f.valoresOriginales(), 
                        f.atributosExtraidos() != null ? f.atributosExtraidos() : Map.of(),
                        "BIEN", "EQUIPO", "NOMBRE_BIEN"))
                .toList();
        
        Optional<AgrupacionDTO> agrupacionDetectada = agrupacionService.detectarAgrupacionPorBienes(nombresBienes);
        
        if (agrupacionDetectada.isPresent()) {
            procesarAgrupacionDetectada(bienes, agrupacionDetectada.get(), resultado);
        } else {
            asignarATodosPeriferico(bienes, resultado);
        }
        
        log.debug("Agrupación inteligente: {} grupos de {} bienes", resultado.size(), bienes.size());
        return resultado;
    }

    private void procesarAgrupacionDetectada(List<FilaPreviewDTO> bienes, AgrupacionDTO agrupacion, 
                                              Map<String, List<FilaPreviewDTO>> resultado) {
        String idGrupo = agrupacion.getId();
        String nombreGrupo = agrupacion.getNombreComponente();
        
        List<FilaPreviewDTO> componentesEquipo = new ArrayList<>();
        List<FilaPreviewDTO> perifericos = new ArrayList<>();
        
        for (FilaPreviewDTO fila : bienes) {
            String nombreBien = obtenerValor(fila.valoresOriginales(), 
                    fila.atributosExtraidos() != null ? fila.atributosExtraidos() : Map.of(),
                    "BIEN", "EQUIPO", "NOMBRE_BIEN");
            
            if (agrupacion.contieneComponente(nombreBien)) {
                componentesEquipo.add(fila.conGrupo(idGrupo, nombreGrupo));
            } else {
                perifericos.add(fila);
            }
        }
        
        if (!componentesEquipo.isEmpty()) {
            resultado.put(idGrupo, componentesEquipo);
        }
        
        if (!perifericos.isEmpty()) {
            asignarPerifericosAGrupo(perifericos, resultado);
        }
    }

    private void asignarPerifericosAGrupo(List<FilaPreviewDTO> perifericos, 
                                           Map<String, List<FilaPreviewDTO>> resultado) {
        Optional<AgrupacionDTO> agrupacionPerifericos = agrupacionService.getAgrupacionPerifericos();
        
        if (agrupacionPerifericos.isPresent()) {
            AgrupacionDTO agrPerif = agrupacionPerifericos.get();
            List<FilaPreviewDTO> perifericosConGrupo = perifericos.stream()
                    .map(p -> p.conGrupo(agrPerif.getId(), agrPerif.getNombreComponente()))
                    .toList();
            resultado.put(agrPerif.getId(), new ArrayList<>(perifericosConGrupo));
        } else {
            int indice = 2;
            for (FilaPreviewDTO periferico : perifericos) {
                resultado.put("PERIFERICO-" + indice++, List.of(periferico));
            }
        }
    }

    private void asignarATodosPeriferico(List<FilaPreviewDTO> bienes, Map<String, List<FilaPreviewDTO>> resultado) {
        Optional<AgrupacionDTO> agrupacionPerifericos = agrupacionService.getAgrupacionPerifericos();
        
        if (agrupacionPerifericos.isPresent()) {
            AgrupacionDTO agrPerif = agrupacionPerifericos.get();
            List<FilaPreviewDTO> todasConGrupo = bienes.stream()
                    .map(f -> f.conGrupo(agrPerif.getId(), agrPerif.getNombreComponente()))
                    .toList();
            resultado.put(agrPerif.getId(), new ArrayList<>(todasConGrupo));
        } else {
            int indice = 1;
            for (FilaPreviewDTO fila : bienes) {
                resultado.put(String.valueOf(indice++), List.of(fila));
            }
        }
    }

    private TipoComponenteEnum determinarTipoGrupo(List<FilaPreviewDTO> filasDelGrupo) {
        if (filasDelGrupo.size() > 1) {
            return TipoComponenteEnum.EQUIPO_COMPLETO;
        }
        
        if (agrupacionService != null) {
            List<String> nombresBienes = filasDelGrupo.stream()
                    .map(f -> obtenerValor(f.valoresOriginales(), 
                            f.atributosExtraidos() != null ? f.atributosExtraidos() : Map.of(),
                            "BIEN", "EQUIPO", "NOMBRE_BIEN"))
                    .toList();
            
            Optional<AgrupacionDTO> agrupacion = agrupacionService.detectarAgrupacionPorBienes(nombresBienes);
            if (agrupacion.isPresent() && "EQUIPO_COMPLETO".equals(agrupacion.get().getTipo())) {
                return TipoComponenteEnum.EQUIPO_COMPLETO;
            }
        }
        
        return clasificarComponentePorCategoria(filasDelGrupo.get(0).categoriaDetectada());
    }

    private String detectarNombreGrupo(List<FilaPreviewDTO> filasDelGrupo) {
        if (agrupacionService == null || filasDelGrupo.isEmpty()) {
            return null;
        }
        
        List<String> nombresBienes = filasDelGrupo.stream()
                .map(f -> obtenerValor(f.valoresOriginales(), 
                        f.atributosExtraidos() != null ? f.atributosExtraidos() : Map.of(),
                        "BIEN", "EQUIPO", "NOMBRE_BIEN"))
                .toList();
        
        Optional<AgrupacionDTO> agrupacion = agrupacionService.detectarAgrupacionPorBienes(nombresBienes);
        if (agrupacion.isPresent()) {
            return agrupacion.get().getNombreComponente();
        }
        
        return agrupacionService.getAgrupacionPerifericos()
                .map(AgrupacionDTO::getNombreComponente)
                .orElse("Periféricos");
    }

    public boolean esNuevaFichaPorPatron(String texto, FichasMultiplesDTO config) {
        if (texto == null || texto.isBlank()) return false;
        if (config == null || !config.habilitadas()) return false;

        var separadores = config.separadores();
        if (separadores == null || separadores.porPatron() == null) return false;
        if (!separadores.porPatron().habilitado()) return false;

        for (String patron : separadores.porPatron().patrones()) {
            try {
                if (texto.matches(patron)) {
                    log.debug("Texto '{}' coincide con patrón de nueva ficha: {}", texto, patron);
                    return true;
                }
            } catch (Exception e) {
                log.warn("Error al evaluar patrón '{}': {}", patron, e.getMessage());
            }
        }

        if (separadores.porPatron().textoExacto() != null) {
            for (String exacto : separadores.porPatron().textoExacto()) {
                if (texto.trim().equals(exacto)) {
                    return true;
                }
            }
        }

        return false;
    }

    public Map<String, List<FilaPreviewDTO>> agruparPorFichas(List<FilaPreviewDTO> filas, FichasMultiplesDTO config) {
        Map<String, List<FilaPreviewDTO>> gruposPorFicha = new LinkedHashMap<>();
        String fichaActual = "FICHA-DEFAULT";

        if (config == null || !config.habilitadas()) {
            gruposPorFicha.put(fichaActual, new ArrayList<>(filas));
            return gruposPorFicha;
        }

        for (FilaPreviewDTO fila : filas) {
            boolean esNuevaFicha = false;
            String nuevoIdFicha = null;

            for (Map.Entry<String, String> entry : fila.valoresOriginales().entrySet()) {
                if (esNuevaFichaPorPatron(entry.getValue(), config)) {
                    esNuevaFicha = true;
                    nuevoIdFicha = entry.getValue();
                    break;
                }
            }

            if (esNuevaFicha && nuevoIdFicha != null) {
                fichaActual = nuevoIdFicha;
            }

            gruposPorFicha.computeIfAbsent(fichaActual, k -> new ArrayList<>()).add(fila);
        }

        log.debug("Agrupación completada: {} ficha(s)", gruposPorFicha.size());
        return gruposPorFicha;
    }
}
