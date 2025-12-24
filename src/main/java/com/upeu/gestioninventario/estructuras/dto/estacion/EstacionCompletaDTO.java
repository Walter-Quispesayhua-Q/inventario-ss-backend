package com.upeu.gestioninventario.estructuras.dto.estacion;

import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstacionCompletaDTO {
    
    // Información básica de la estación
    private Long idEstacion;
    private String nombre;
    private String codigo;
    private Integer capacidadBienes;
    private Long cantidadBienesAsignados;
    private Integer capacidadDisponible;
    private String observaciones;
    private Map<String, Object> propiedadesAdicionales;
    
    // Información del tipo de estructura
    private TipoEstructuraDTO tipoEstructura;
    
    // Información completa de ubicación anidada
    private UbicacionCompletaDTO ubicacion;
    
    // Fechas de auditoría completas
    private AuditoriaCompletaDTO auditoria;
    
    // Todos los bienes individuales asignados a esta estación
    private List<BienEstacionCompletaDTO> bienesAsignados;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UbicacionCompletaDTO {
        
        // Información del ambiente
        private AmbienteCompletaDTO ambiente;
        
        // Información del piso
        private PisoCompletaDTO piso;
        
        // Información del edificio
        private EdificioCompletaDTO edificio;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AmbienteCompletaDTO {
            private Long idAmbiente;
            private String nombre;
            private String codigo;
            private Integer capacidadPersonas;
            private TipoEstructuraDTO tipoEstructura;
            private String observaciones;
            private Map<String, Object> propiedadesAdicionales;
            
            // Responsable del ambiente
            private UsuarioDTO responsable;
            private DepartamentoDTO departamentoResponsable;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PisoCompletaDTO {
            private Long idPiso;
            private String nombre;
            private String codigo;
            private Integer numeroPiso;
            private TipoEstructuraDTO tipoEstructura;
            private String observaciones;
            private Map<String, Object> propiedadesAdicionales;
            
            // Responsable del piso
            private UsuarioDTO responsableMantenimiento;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EdificioCompletaDTO {
            private Long idEdificio;
            private String nombre;
            private String codigo;
            private String direccion;
            private Integer numeroPisos;
            private java.math.BigDecimal areaTotalM2;
            private Integer anoConstruccion;
            private TipoEstructuraDTO tipoEstructura;
            private String observaciones;
            private Map<String, Object> propiedadesAdicionales;
            
            // Responsable del edificio
            private UsuarioDTO responsableMantenimiento;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditoriaCompletaDTO {
        
        // Fechas de auditoría
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaUltimaModificacion;
        private LocalDateTime fechaEliminacion;
        
        // Información completa del responsable de creación
        private UsuarioDTO usuarioCreacion;
        
        // Información completa del responsable de última modificación
        private UsuarioDTO usuarioUltimaModificacion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BienEstacionCompletaDTO {
        
        // Información de la asignación
        private Long idAsignacion;
        private String posicionRelativa;
        private LocalDateTime fechaAsignacion;
        private LocalDateTime fechaDesasignacion;
        private String observaciones;
        private Boolean asignacionActiva;
        
        // Usuario que realizó la asignación
        private UsuarioDTO usuarioAsignacion;
        
        // Información completa del bien
        private BienCompletoDTO bien;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BienCompletoDTO {
            private Long idBien;
            private String nombre;
            private String numeroSerie;
            private String caf;
            private String marca;
            private String modelo;
            private String color;
            private java.math.BigDecimal valorUnitario;
            private String estado;
            private String observaciones;
            
            // Categoría del bien
            private CategoriaDTO categoria;
            
            // Fechas de auditoría del bien
            private LocalDateTime fechaCreacion;
            private LocalDateTime fechaUltimaModificacion;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioDTO {
        private Long idUsuario;
        private String nombre;
        private String apellido;
        private String email;
        private String nombreCompleto;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartamentoDTO {
        private Long idDepartamento;
        private String nombre;
        private String codigo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaDTO {
        private Long idCategoria;
        private String nombre;
        private String codigo;
    }
}
