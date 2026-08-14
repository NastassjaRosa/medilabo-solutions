package com.medilabo.patientservice.config;

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
 * Vérifie les règles d'authentification de patient-service : deny-by-default sur les
 * routes protégées, accès public uniquement sur {@code /actuator/health}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PatientSecurityTest {

    private static final String AUTH_USERNAME = "test-user";
    private static final String AUTH_PASSWORD = "test-password";

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("GATEWAY_AUTH_USERNAME", () -> AUTH_USERNAME);
        registry.add("GATEWAY_AUTH_PASSWORD", () -> AUTH_PASSWORD);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:patient-security-test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @Test
    void healthEstAccessibleSansAuthentification() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void uneRouteProtegeeSansAuthentificationRenvoie401() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uneRouteProtegeeAvecDeMauvaisIdentifiantsRenvoie401() throws Exception {
        mockMvc.perform(get("/patients")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic(AUTH_USERNAME, "mot-de-passe-incorrect")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uneRouteProtegeeAvecLeBonCompteRenvoie200() throws Exception {
        mockMvc.perform(get("/patients")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic(AUTH_USERNAME, AUTH_PASSWORD)))
                .andExpect(status().isOk());
    }
}
