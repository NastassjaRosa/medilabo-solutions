package com.medilabo.patientservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du microservice patient (dossier démographique, base MySQL).
 *
 * @since 1.0
 */
@SpringBootApplication
public class PatientServiceApplication {

    /**
     * Démarre le microservice.
     *
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(PatientServiceApplication.class, args);
    }
}
