package com.upeu.gestioninventario.shared.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Configuración de GraphQL con scalars extendidos.
 * <p>
 * Nota: Los archivos se manejan por REST API (POST /api/upload), no por GraphQL.
 * Solo se configuran scalars para datos estructurados (JSON, DateTime, Date, Long).
 */
@Configuration
public class GraphqlConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.Json)
                .scalar(ExtendedScalars.DateTime)
                .scalar(ExtendedScalars.Date)
                .scalar(ExtendedScalars.GraphQLLong);
    }
}
