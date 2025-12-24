package com.upeu.gestioninventario.shared.utils.naming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class EstandarizadorNombresService {

    private static final Set<String> CLAVES_NOMBRE_BIEN = Set.of(
            "bien", "nombre", "nombre del bien", "item", "descripcion",
            "descripción", "equipo", "articulo", "artículo", "item name"
    );

    public String generarNombreParaBien(Map<String, String> atributosExtraidos, String nombreOriginal) {
        if (atributosExtraidos == null || atributosExtraidos.isEmpty()) {
            return limpiar(nombreOriginal);
        }

        String nombreBase = extraerNombreBase(atributosExtraidos);

        if (nombreBase == null || nombreBase.isBlank()) {
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

    public String obtenerNombreCategoriaPlural(String nombreSingular) {
        if (nombreSingular == null || nombreSingular.isBlank()) return "Desconocidas";
        if (nombreSingular.endsWith("r") || nombreSingular.endsWith("n") || nombreSingular.endsWith("l")) {
            return nombreSingular + "es";
        }
        if (nombreSingular.endsWith("z")) {
            return nombreSingular.substring(0, nombreSingular.length() - 1) + "ces";
        }
        return nombreSingular + "s";
    }

    public String obtenerNombreCategoriaSingular(String nombrePlural) {
        if (nombrePlural == null || nombrePlural.isBlank()) return "Desconocida";
        String lower = nombrePlural.toLowerCase();
        if (lower.endsWith("ces")) {
            return nombrePlural.substring(0, nombrePlural.length() - 3) + "z";
        }
        if (lower.endsWith("es")) {
            return nombrePlural.substring(0, nombrePlural.length() - 2);
        }
        if (lower.endsWith("s")) {
            return nombrePlural.substring(0, nombrePlural.length() - 1);
        }
        return nombrePlural;
    }
}