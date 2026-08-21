package com.medilabo.riskservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Point d'entrée du microservice d'évaluation du risque de diabète de type 2.
 * Ce service n'a pas de base de données propre : il interroge {@code patient-service}
 * et {@code notes-service} via des clients OpenFeign.
 *
 * @since 1.0
 */
@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan
public class RiskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskServiceApplication.class, args);
    }
}
