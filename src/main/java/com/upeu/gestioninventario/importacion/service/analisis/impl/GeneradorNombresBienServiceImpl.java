package com.upeu.gestioninventario.importacion.service.analisis.impl;

import com.upeu.gestioninventario.importacion.service.analisis.IGeneradorNombresBien;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class GeneradorNombresBienServiceImpl implements IGeneradorNombresBien {
    private static final Set<String> CLAVES_NOMBRE_BIEN = Set.of(
            "bien", "nombre", "nombre del bien", "item", "descripcion",
            "descripción", "equipo", "articulo", "artículo", "item name"
    );

    @Override
    public String generarNombre(Map<String, String> atributosExtraidos, String nombreOriginal) {
        if (atributosExtraidos == null || atributosExtraidos.isEmpty()) {
            log.debug("Sin atributos, usando nombre original");
            return limpiar(nombreOriginal);
        }

        String nombreBase = extraerNombreBase(atributosExtraidos);

        if (nombreBase == null || nombreBase.isBlank()) {
            log.debug("No se encontró nombre base, usando original");
            return limpiar(nombreOriginal);
        }

        return construirNombreCompleto(nombreBase, atributosExtraidos);
    }

    private String extraerNombreBase(Map<String, String> atributos) {

        String nombreBien = atributos.get("NOMBRE_BIEN");

        if (esNombreValido(nombreBien)) {
            return nombreBien.trim();
        }

        for (Map.Entry<String, String> entry : atributos.entrySet()) {
            String clave = entry.getKey().toLowerCase().trim();
            if (CLAVES_NOMBRE_BIEN.contains(clave) && esNombreValido(entry.getValue())) {
                return entry.getValue().trim();
            }
        }

        return null;
    }

    private boolean esNombreValido(String nombre) {
        if (nombre == null || nombre.isBlank()) return false;
        int espacios = nombre.length() - nombre.replace(" ", "").length();
        return nombre.length() <= 80 && espacios <= 8;
    }

    private String construirNombreCompleto(String nombreBase, Map<String, String> atributos) {
        StringBuilder nombre = new StringBuilder(nombreBase);

        String marca = atributos.get("MARCA");
        String modelo = atributos.get("MODELO");

        if (marca != null && !marca.isBlank() && !contieneIgnoreCase(nombreBase, marca)) {
            nombre.append(" ").append(marca.trim());
        }

        if (modelo != null && !modelo.isBlank() && !contieneIgnoreCase(nombreBase, modelo)) {
            nombre.append(" ").append(modelo.trim());
        }

        return limpiar(nombre.toString());
    }

    private boolean contieneIgnoreCase(String texto, String buscar) {
        return texto.toLowerCase().contains(buscar.toLowerCase());
    }

    private String limpiar(String texto) {
        if (texto == null) return "Sin Nombre";
        return texto.replaceAll("\\s+", " ").trim();
    }

    @Override
    public String obtenerNombrePlural(String nombreSingular) {
        if (nombreSingular == null || nombreSingular.isBlank()) {
            return "Desconocidas";
        }
        String nombre = nombreSingular.trim();
        if (nombre.endsWith("r") || nombre.endsWith("n") || nombre.endsWith("l")) {
            return nombre + "es";
        }
        if (nombre.endsWith("z")) {
            return nombre.substring(0, nombre.length() - 1) + "ces";
        }
        if (nombre.endsWith("s") || nombre.endsWith("x")) {
            return nombre;
        }
        return nombre + "s";
    }

    @Override
    public boolean contienePalabra(String texto, String palabra) {
        if (texto == null || palabra == null) return false;
        return Arrays.stream(texto.toLowerCase().split("\\s+"))
                .anyMatch(p -> p.equals(palabra.toLowerCase()));
    }
}