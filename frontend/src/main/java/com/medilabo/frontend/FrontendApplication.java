package com.medilabo.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entree du microservice front (interface Thymeleaf) - MediLabo Solutions.
 *
 * @since 1.0
 */
@SpringBootApplication
public class FrontendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}
