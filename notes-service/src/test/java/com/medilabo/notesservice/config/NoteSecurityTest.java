package com.medilabo.notesservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie les règles d'authentification de notes-service : deny-by-default sur les
 * routes protégées, accès public uniquement sur {@code /actuator/health}. La base
 * MongoDB utilisée est celle démarrée automatiquement pour les tests (Flapdoodle),
 * {@code spring.data.mongodb.uri} n'étant pas fourni ici.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NoteSecurityTest {

    private static final String AUTH_USERNAME = "test-user";
    private static final String AUTH_PASSWORD = "test-password";

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("GATEWAY_AUTH_USERNAME", () -> AUTH_USERNAME);
        registry.add("GATEWAY_AUTH_PASSWORD", () -> AUTH_PASSWORD);
    }

    @Test
    void healthEstAccessibleSansAuthentification() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void uneRouteProtegeeSansAuthentificationRenvoie401() throws Exception {
        mockMvc.perform(get("/notes/patient/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uneRouteProtegeeAvecDeMauvaisIdentifiantsRenvoie401() throws Exception {
        mockMvc.perform(get("/notes/patient/1")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic(AUTH_USERNAME, "mot-de-passe-incorrect")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uneRouteProtegeeAvecLeBonCompteRenvoie200() throws Exception {
        mockMvc.perform(get("/notes/patient/1")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic(AUTH_USERNAME, AUTH_PASSWORD)))
                .andExpect(status().isOk());
    }
}
