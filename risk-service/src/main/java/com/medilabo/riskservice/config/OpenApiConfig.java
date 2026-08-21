package com.medilabo.riskservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation OpenAPI (springdoc) du microservice de risque, avec le schéma
 * d'authentification HTTP Basic requis par tous les endpoints protégés.
 *
 * @since 1.0
 */
@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH_SCHEME = "basicAuth";

    /**
     * Décrit l'API exposée par risk-service pour Swagger UI.
     *
     * @return la définition OpenAPI du service
     */
    @Bean
    public OpenAPI riskServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("risk-service API")
                        .description("Évaluation du risque de diabète de type 2 à partir des données patient et des notes médicales.")
                        .version("1.0"))
                .components(new Components().addSecuritySchemes(BASIC_AUTH_SCHEME,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH_SCHEME));
    }
}
