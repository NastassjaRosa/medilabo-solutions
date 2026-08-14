package com.medilabo.notesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du microservice notes (comptes rendus de visite, base MongoDB).
 *
 * @since 1.0
 */
@SpringBootApplication
public class NotesServiceApplication {

    /**
     * Démarre le microservice.
     *
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(NotesServiceApplication.class, args);
    }
}
