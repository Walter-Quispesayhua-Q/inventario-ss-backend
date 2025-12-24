package com.upeu.gestioninventario.shared.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "inventario.inicializacion")
@Getter
@Setter
public class InicializacionProperties {

    private SincronizarPlantillas sincronizarPlantillas = new SincronizarPlantillas();

    @Getter
    @Setter
    public static class SincronizarPlantillas {
        /**
         * Habilita la sincronización de plantillas desde archivos JSON a la BD al iniciar.
         * El sistema de hash se encarga de detectar automáticamente qué archivos cambiaron.
         */
        private boolean habilitado = true;
    }
}