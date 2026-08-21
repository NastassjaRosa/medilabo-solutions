package com.medilabo.riskservice.config;

import com.medilabo.riskservice.client.NotesClient;
import com.medilabo.riskservice.client.PatientClient;
import com.medilabo.riskservice.client.dto.Genre;
import com.medilabo.riskservice.client.dto.PatientDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie les règles d'authentification de risk-service : deny-by-default sur les
 * routes protégées, accès public uniquement sur {@code /actuator/health}. Les
 * clients Feign sont simulés pour permettre un aller-retour HTTP complet (chaîne
 * de sécurité réelle) sans dépendre de patient-service/notes-service.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RiskSecurityTest {

    private static final String AUTH_USERNAME = "test-user";
    private static final String AUTH_PASSWORD = "test-password";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientClient patientClient;

    @MockBean
    private NotesClient notesClient;

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
        mockMvc.perform(get("/risk/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uneRouteProtegeeAvecDeMauvaisIdentifiantsRenvoie401() throws Exception {
        mockMvc.perform(get("/risk/1")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic(AUTH_USERNAME, "mot-de-passe-incorrect")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uneRouteProtegeeAvecLeBonCompteRenvoie200() throws Exception {
        given(patientClient.getById(1L)).willReturn(
                new PatientDTO(1L, "Test", "TestNone", LocalDate.of(1966, 12, 31), Genre.F,
                        "1 Brookside St", "100-222-3333"));
        given(notesClient.getByPatientId(1L)).willReturn(List.of());

        mockMvc.perform(get("/risk/1")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic(AUTH_USERNAME, AUTH_PASSWORD)))
                .andExpect(status().isOk());
    }
}
