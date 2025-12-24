package com.upeu.gestioninventario.shared.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "inventario.plantillas")
@Getter
@Setter
public class PlantillasProperties {

    /** Directorio base donde se encuentran los archivos JSON de las plantillas. */
    private String directorio = "seed/plantillas";

    /** Nombre del archivo de configuración principal que define los grupos y convenciones. */
    private String config = "_config/index.json";
}