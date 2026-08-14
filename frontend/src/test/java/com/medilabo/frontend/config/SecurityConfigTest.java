package com.medilabo.frontend.config;

import com.medilabo.frontend.client.PatientGatewayClient;
import com.medilabo.frontend.dto.PatientDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifie les regles d'authentification du front : deny-by-default avec redirection vers le
 * formulaire de connexion, page de connexion publique, CSRF exige sur les formulaires
 * d'ecriture, acces autorise une fois authentifie.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "FRONTEND_AUTH_USERNAME=test-user",
        "FRONTEND_AUTH_PASSWORD=test-password",
        "GATEWAY_AUTH_USERNAME=test-gw-user",
        "GATEWAY_AUTH_PASSWORD=test-gw-password"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientGatewayClient patientGatewayClient;

    @Test
    void accesAnonymeRedirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void laPageDeConnexionEstAccessibleSansAuthentification() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void accesAuthentifieEstAutorise() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void ecritureSansJetonCsrfEstRejetee() throws Exception {
        mockMvc.perform(post("/patients")
                        .param("prenom", "Jean")
                        .param("nom", "Dupont")
                        .param("dateNaissance", "1980-01-01")
                        .param("genre", "M"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void ecritureAvecJetonCsrfEstAcceptee() throws Exception {
        PatientDTO created = new PatientDTO();
        created.setId(1L);
        when(patientGatewayClient.create(any(PatientDTO.class))).thenReturn(created);

        mockMvc.perform(post("/patients")
                        .with(csrf())
                        .param("prenom", "Jean")
                        .param("nom", "Dupont")
                        .param("dateNaissance", "1980-01-01")
                        .param("genre", "M"))
                .andExpect(status().is3xxRedirection());
    }
}
