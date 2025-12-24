package com.upeu.gestioninventario.shared.services.rules;

import com.upeu.gestioninventario.ml.dto.seed.AtributoExtraccionSeedDTO;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CommonAttributeRules {

    private Set<String> nombresCamposBaseCache;

    public List<AtributoExtraccionSeedDTO> getCommonRules() {
        return List.of(
                createRule("NOMBRE_BIEN", "Nombre del Bien", "TEXTO", true, 1, "Ej: Monitor LED 24 pulgadas", Map.of(
                        "estrategias", List.of(
                                Map.of("tipo", "COLUMNA_MAPEADA", "nombresColumna",
                                        Arrays.asList("Nombre", "Nombre del Bien", "Item Name", "Bien", "Descripcion", "Descripción", "Item", "Equipo"),
                                        "prioridad", 1),
                                Map.of("tipo", "PATRON", "patron", "^(?:[\\d\\s.-]+)?([a-zA-Z].*?)(?=\\s*(?:\\d{2}(?:\\.\\d{1,2})?\"|Ma:|Mo:|Se:|$))", "grupo", 1, "prioridad", 2)
                        )
                )),
                createRule("CAF", "Código de Activo Fijo", "TEXTO", false, 2, "Ej: 51438 o P/L 12345", Map.of(
                        "estrategias", List.of(
                                Map.of("tipo", "COLUMNA_MAPEADA", "nombresColumna", List.of("CAF", "Codigo", "Código", "Cod. Activo", "Activo Fijo", "Asset Code"), "prioridad", 1),
                                Map.of("tipo", "PATRON", "patron", "\\b(CAF[-\\s]?[A-Z0-9]+)\\b", "grupo", 1),
                                Map.of("tipo", "PATRON", "patron", "^([A-Z0-9]{3,}-[A-Z0-9]{3,}-[A-Z0-9]+)", "grupo", 1)
                        )
                )),
                createRule("NUMERO_SERIE", "Número de Serie", "TEXTO", false, 3, "Ej: SVNA7BHPC", Map.of(
                        "estrategias", List.of(
                                Map.of("tipo", "COLUMNA_MAPEADA", "nombresColumna", List.of("Serie", "Serial", "Número de Serie", "S/N", "Serial Number", "Nro Serie", "Se"), "prioridad", 1),
                                Map.of("tipo", "ETIQUETA", "patron", "(se:|serie:|serial:)\\s*([A-Z0-9]+)", "grupo", 2),
                                Map.of("tipo", "PATRON", "patron", "\\b[A-Z]{2,4}[0-9]{5,}\\b")
                        )
                )),
                createRule("UBICACION", "Ubicación", "TEXTO", false, 10, "Ej: Laboratorio de Software, Oficina 301", Map.of(
                        "estrategias", List.of(
                                Map.of("tipo", "COLUMNA_MAPEADA", "nombresColumna", List.of("Ubicación", "Ubicacion", "Location", "Oficina", "Área", "Departamento", "Ubicación actual"), "prioridad", 1),
                                Map.of("tipo", "ETIQUETA", "patron", "(ubicacion:|ubicación:|ub:)\\s*([\\w\\s]+)", "grupo", 2)
                        )
                )),
                createRule("OBSERVACIONES", "Observaciones", "TEXTO_LARGO", false, 100, "Añada cualquier nota o comentario relevante aquí...", Map.of(
                        "estrategias", List.of(
                                Map.of("tipo", "COLUMNA_MAPEADA", "nombresColumna", Arrays.asList("Observaciones", "Observacion", "Notas", "Comentarios", "Notes", "Comments", "Obs", "Observación - descargo"), "prioridad", 1),
                                Map.of("tipo", "ETIQUETA", "patron", "(obs:|observaciones:|notas:|comentarios:)\\s*(.+)", "grupo", 2),
                                Map.of("tipo", "PATRON", "patron", "\\[(.+?)\\]$", "grupo", 1, "descripcion", "Texto entre corchetes al final"),
                                Map.of("tipo", "RESIDUAL", "descripcion", "Captura texto no procesado como observación")
                        )
                )),
                createRule("ESTADO_FISICO", "Estado Físico", "LISTA", false, 98, "Seleccione el estado físico", Map.of(
                        "opciones", List.of("Nuevo", "Bueno", "Regular", "Malo"),
                        "estrategias", List.of(
                                Map.of("tipo", "COLUMNA_MAPEADA", "nombresColumna", List.of("Estado", "Condición", "Estado Físico"), "prioridad", 1),
                                Map.of("tipo", "PALABRAS", "lista", List.of("nuevo", "bueno", "regular", "malo", "excelente", "usado", "deteriorado"), "mapeo", Map.of(
                                        "excelente", "Nuevo",
                                        "usado", "Regular",
                                        "deteriorado", "Malo"
                                ))
                        )
                )),
                createRule("ESTADO_OPERACIONAL", "Estado Operacional", "LISTA", true, 99, "Seleccione el estado de uso", Map.of(
                        "opciones", List.of("Operativo", "En Reparación", "Almacenado", "De Baja"),
                        "estrategias", List.of(
                                Map.of("tipo", "COLUMNA_MAPEADA", "nombresColumna", List.of("Estado Actual", "Estado Operacional", "Estado de Uso"), "prioridad", 1),
                                Map.of("tipo", "PALABRAS", "lista", List.of("operativo", "funcionando", "activo", "reparacion", "reparación", "mantenimiento", "almacenado", "guardado", "bodega", "baja", "descartado", "inservible"), "mapeo", Map.ofEntries(
                                        Map.entry("funcionando", "Operativo"),
                                        Map.entry("activo", "Operativo"),
                                        Map.entry("reparacion", "En Reparación"),
                                        Map.entry("reparación", "En Reparación"),
                                        Map.entry("mantenimiento", "En Reparación"),
                                        Map.entry("guardado", "Almacenado"),
                                        Map.entry("bodega", "Almacenado"),
                                        Map.entry("descartado", "De Baja"),
                                        Map.entry("inservible", "De Baja")
                                ))
                        )
                )),
                createRule("DESCRIPCION_COMPLETA", "Descripción Original", "TEXTO", false, 101, "Texto original del bien, ej: 01 Monitor Ma: Lenovo...", Map.of(
                        "estrategias", List.of(
                                // Usamos Arrays.asList() porque hay más de 10 elementos
                                Map.of("tipo", "COLUMNA_MAPEADA", "nombresColumna", Arrays.asList("Descripción", "Description", "Detalle", "Nombre", "Item", "Detalle del bien"), "prioridad", 1),
                                Map.of("tipo", "TEXTO_COMPLETO")
                        )
                ))
        );
    }

    private AtributoExtraccionSeedDTO createRule(String nombreCampo, String etiqueta, String tipoDato, boolean requerido, int orden, String placeholder, Map<String, Object> extraccionJson) {
        return new AtributoExtraccionSeedDTO(
                nombreCampo,
                etiqueta,
                tipoDato,
                requerido,
                null, // prioridad (no se usa aquí)
                orden,
                placeholder,
                null, // validacion
                null, // opciones
                extraccionJson,
                null, // valorPorDefecto
                null  // maxLength
        );
    }

    /**
     * Comprueba si un nombre de campo corresponde a un atributo base definido en esta clase.
     * Utiliza una caché para ser más eficiente.
     * @param nombreCampo El nombre del campo a comprobar.
     * @return true si es un campo base, false en caso contrario.
     */
    public boolean esCampoBase(String nombreCampo) {
        if (nombresCamposBaseCache == null) {
            // Carga la caché la primera vez que se llama al método
            nombresCamposBaseCache = getCommonRules().stream()
                    .map(AtributoExtraccionSeedDTO::nombreCampo)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            log.info("Caché de nombres de campos base inicializada con {} elementos.", nombresCamposBaseCache.size());
        }
        return nombresCamposBaseCache.contains(nombreCampo);
    }
}
