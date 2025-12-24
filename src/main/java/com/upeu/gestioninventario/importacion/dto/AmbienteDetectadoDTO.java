package com.upeu.gestioninventario.importacion.dto;

public record AmbienteDetectadoDTO(
        // Ambiente
        Long idAmbiente,
        String nombreAmbiente,
        String codigoAmbiente,
        
        // Piso
        Long idPiso,
        String nombrePiso,
        Integer numeroPiso,
        
        // Edificio
        Long idEdificio,
        String nombreEdificio,
        
        // Metadata de detección
        double porcentajeCoincidencia,
        String textoOriginalUbicacion
) {
    public static AmbienteDetectadoDTO noDetectado() {
        return new AmbienteDetectadoDTO(
                null, null, null,
                null, null, null,
                null, null,
                0.0, null
        );
    }
    
    /**
     * Factory method para crear con el texto original cuando no se encuentra coincidencia
     */
    public static AmbienteDetectadoDTO noDetectadoConTexto(String textoOriginal) {
        return new AmbienteDetectadoDTO(
                null, null, null,
                null, null, null,
                null, null,
                0.0, textoOriginal
        );
    }

    public boolean tieneAmbiente() {
        return idAmbiente != null;
    }

    public String obtenerJerarquiaCompleta() {
        if (!tieneAmbiente()) return "Sin ubicación";
        return String.format("%s > %s > %s", nombreEdificio, nombrePiso, nombreAmbiente);
    }
}

