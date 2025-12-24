package com.upeu.gestioninventario.shared.utils;

import java.util.regex.Pattern;

/**
 * Utilidades para manipulación de cadenas de texto.
 * <p>
 * Proporciona conversiones de formato como camelCase a snake_case.
 */
public final class StringUtils {

    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("(?<=[a-z])(?=[A-Z])");

    private StringUtils() {
        throw new UnsupportedOperationException("Clase de utilidades no instanciable");
    }

    /**
     * Convierte una cadena en formato camelCase a snake_case en minúsculas.
     * <p>
     * Ejemplo: "nombreCompleto" → "nombre_completo"
     */
    public static String toSnakeCase(String str) {
        if (str == null) {
            return null;
        }
        return SNAKE_CASE_PATTERN.matcher(str).replaceAll("_").toLowerCase();
    }

    /**
     * Convierte una cadena en formato camelCase a SNAKE_CASE en mayúsculas.
     * <p>
     * Ejemplo: "nombreCompleto" → "NOMBRE_COMPLETO"
     */
    public static String toUpperSnakeCase(String str) {
        return toSnakeCase(str)!= null ? toSnakeCase(str).toUpperCase() : null;
    }
}