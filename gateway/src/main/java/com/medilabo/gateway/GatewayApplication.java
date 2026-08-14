package com.medilabo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entree du microservice gateway : seul service exposant un port vers
 * l'exterieur, il route le trafic entrant vers les microservices back et le front.
 *
 * @since 1.0
 */
@SpringBootApplication
public class GatewayApplication {

    /**
     * Demarre le contexte Spring Boot de la gateway.
     *
     * @param args arguments de ligne de commande transmis a Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
