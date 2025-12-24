package com.upeu.gestioninventario.importacion.service.analisis.impl;

import com.upeu.gestioninventario.estructuras.model.Ambiente;
import com.upeu.gestioninventario.estructuras.model.Edificio;
import com.upeu.gestioninventario.estructuras.model.Piso;
import com.upeu.gestioninventario.estructuras.repository.AmbienteRepository;
import com.upeu.gestioninventario.importacion.dto.AmbienteDetectadoDTO;
import com.upeu.gestioninventario.importacion.service.analisis.IBuscadorAmbiente;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuscadorAmbienteServiceImpl implements IBuscadorAmbiente {

    private final AmbienteRepository ambienteRepository;

    @Override
    public Optional<ResultadoBusqueda> buscarPorSimilitud(String textoUbicacion, double umbralMinimo) {
        if (textoUbicacion == null || textoUbicacion.isBlank()) {
            return Optional.empty();
        }

        String textoNormalizado = normalizar(textoUbicacion);
        log.debug("Buscando ambiente para: '{}' (normalizado: '{}')", textoUbicacion, textoNormalizado);

        List<Ambiente> todosAmbientes = ambienteRepository.findAll();

        ResultadoBusqueda mejorResultado = null;
        double mejorPorcentaje = 0.0;

        for (Ambiente ambiente : todosAmbientes) {
            double porcentaje = calcularSimilitud(textoNormalizado, normalizar(ambiente.getNombre()));

            if (porcentaje > mejorPorcentaje) {
                mejorPorcentaje = porcentaje;
                mejorResultado = new ResultadoBusqueda(
                        ambiente,
                        porcentaje,
                        textoUbicacion,
                        ambiente.getNombre()
                );
            }
        }

        if (mejorResultado != null && mejorPorcentaje >= umbralMinimo) {
            log.info("Ambiente encontrado: '{}' con {}% de coincidencia",
                    mejorResultado.nombreAmbienteCoincidente(),
                    String.format("%.1f", mejorPorcentaje * 100));
            return Optional.of(mejorResultado);
        }

        log.debug("No se encontró ambiente con coincidencia >= {}%", umbralMinimo * 100);
        return Optional.empty();
    }

    @Override
    public double calcularSimilitud(String texto1, String texto2) {
        if (texto1 == null || texto2 == null) return 0.0;
        if (texto1.equals(texto2)) return 1.0;

        Set<String> palabras1 = extraerPalabras(texto1);
        Set<String> palabras2 = extraerPalabras(texto2);

        if (palabras1.isEmpty() || palabras2.isEmpty()) return 0.0;

        Set<String> interseccion = new HashSet<>(palabras1);
        interseccion.retainAll(palabras2);

        Set<String> union = new HashSet<>(palabras1);
        union.addAll(palabras2);

        double similitudJaccard = (double) interseccion.size() / union.size();

        int coincidenciasExactas = interseccion.size();
        int totalPalabrasReferencia = Math.max(palabras1.size(), palabras2.size());
        double similitudPorcentual = (double) coincidenciasExactas / totalPalabrasReferencia;

        return (similitudJaccard + similitudPorcentual) / 2.0;
    }

    @Override
    public AmbienteDetectadoDTO buscarAmbienteDTO(String textoUbicacion, double umbralMinimo) {
        if (textoUbicacion == null || textoUbicacion.isBlank()) {
            return AmbienteDetectadoDTO.noDetectado();
        }

        Optional<ResultadoBusqueda> resultado = buscarPorSimilitud(textoUbicacion, umbralMinimo);

        if (resultado.isEmpty()) {
            log.debug("No se encontró ambiente para: '{}'", textoUbicacion);
            // Retornar con el texto original para que el frontend pueda mostrarlo
            return AmbienteDetectadoDTO.noDetectadoConTexto(textoUbicacion);
        }

        ResultadoBusqueda busqueda = resultado.get();
        Ambiente ambiente = busqueda.ambiente();

        // Extraer información del Piso
        Long idPiso = null;
        String nombrePiso = null;
        Integer numeroPiso = null;
        
        // Extraer información del Edificio
        Long idEdificio = null;
        String nombreEdificio = null;

        Piso piso = ambiente.getPiso();
        if (piso != null) {
            idPiso = piso.getIdPiso();
            nombrePiso = piso.getNombre();
            numeroPiso = piso.getNumeroPiso();
            
            Edificio edificio = piso.getEdificio();
            if (edificio != null) {
                idEdificio = edificio.getIdEdificio();
                nombreEdificio = edificio.getNombre();
            }
        }

        log.info("Ambiente detectado: {} > {} > {} ({}% coincidencia)",
                nombreEdificio, nombrePiso, ambiente.getNombre(),
                String.format("%.1f", busqueda.porcentajeCoincidencia() * 100));

        return new AmbienteDetectadoDTO(
                ambiente.getIdAmbiente(),
                ambiente.getNombre(),
                ambiente.getCodigo(),
                idPiso,
                nombrePiso,
                numeroPiso,
                idEdificio,
                nombreEdificio,
                busqueda.porcentajeCoincidencia(),
                textoUbicacion
        );
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Set<String> extraerPalabras(String texto) {
        if (texto == null || texto.isBlank()) return Set.of();

        Set<String> palabrasRelevantes = new HashSet<>();
        String[] palabras = texto.split("\\s+");

        Set<String> stopWords = Set.of(
                "de", "del", "la", "el", "los", "las", "en", "a", "y", "e", "o", "u",
                "un", "una", "unos", "unas", "para", "por", "con", "sin"
        );

        for (String palabra : palabras) {
            if (palabra.length() > 2 && !stopWords.contains(palabra)) {
                palabrasRelevantes.add(palabra);
            }
        }

        return palabrasRelevantes;
    }
}
