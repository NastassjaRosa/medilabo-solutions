package com.medilabo.frontend.controller;

import com.medilabo.frontend.client.PatientGatewayClient;
import com.medilabo.frontend.dto.Genre;
import com.medilabo.frontend.dto.PatientDTO;
import com.medilabo.frontend.exception.GatewayValidationException;
import com.medilabo.frontend.exception.PatientNotFoundException;
import com.medilabo.frontend.exception.UiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Verifie le rendu des vues patient (liste/detail/formulaires), l'echappement XSS via
 * Thymeleaf, la validation des formulaires et le mapping des exceptions du client gateway.
 */
@WebMvcTest(controllers = {PatientUiController.class})
@Import(UiExceptionHandler.class)
@TestPropertySource(properties = {
        "FRONTEND_AUTH_USERNAME=test-user",
        "FRONTEND_AUTH_PASSWORD=test-password"
})
class PatientUiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientGatewayClient patientGatewayClient;

    @Test
    @WithMockUser
    void laListeEchappeLesDonneesPourEviterLeXss() throws Exception {
        PatientDTO patient = new PatientDTO();
        patient.setId(1L);
        patient.setNom("<script>alert(1)</script>");
        patient.setPrenom("Test");
        patient.setDateNaissance(LocalDate.of(1980, 1, 1));
        patient.setGenre(Genre.F);
        when(patientGatewayClient.findAll()).thenReturn(List.of(patient));

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/list"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")));
    }

    @Test
    @WithMockUser
    void leDetailAffichePatientTrouve() throws Exception {
        PatientDTO patient = new PatientDTO();
        patient.setId(1L);
        patient.setNom("Dupont");
        patient.setPrenom("Jean");
        patient.setDateNaissance(LocalDate.of(1980, 1, 1));
        patient.setGenre(Genre.M);
        when(patientGatewayClient.findById(1L)).thenReturn(patient);

        mockMvc.perform(get("/patients/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/detail"))
                .andExpect(model().attribute("patient", patient));
    }

    @Test
    @WithMockUser
    void leDetailRenvoie404SiPatientIntrouvable() throws Exception {
        when(patientGatewayClient.findById(99L)).thenThrow(new PatientNotFoundException(99L));

        mockMvc.perform(get("/patients/99"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    @WithMockUser
    void leFormulaireDAjoutEstVide() throws Exception {
        mockMvc.perform(get("/patients/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().attribute("mode", "create"));
    }

    @Test
    @WithMockUser
    void laCreationAvecPrenomManquantReaffichheLeFormulaireAvecErreur() throws Exception {
        mockMvc.perform(post("/patients")
                        .with(csrf())
                        .param("nom", "Dupont")
                        .param("dateNaissance", "1980-01-01")
                        .param("genre", "M"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().attributeHasFieldErrors("patient", "prenom"));
    }

    @Test
    @WithMockUser
    void laCreationValideRedirigeVersLeDetail() throws Exception {
        PatientDTO created = new PatientDTO();
        created.setId(42L);
        when(patientGatewayClient.create(any(PatientDTO.class))).thenReturn(created);

        mockMvc.perform(post("/patients")
                        .with(csrf())
                        .param("prenom", "Jean")
                        .param("nom", "Dupont")
                        .param("dateNaissance", "1980-01-01")
                        .param("genre", "M"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/42"));
    }

    @Test
    @WithMockUser
    void laCreationRejeteeParLePatientServiceReaffichheLeFormulaireAvecLesErreursDistantes() throws Exception {
        when(patientGatewayClient.create(any(PatientDTO.class)))
                .thenThrow(new GatewayValidationException(Map.of("nom", "Le nom est obligatoire")));

        mockMvc.perform(post("/patients")
                        .with(csrf())
                        .param("prenom", "Jean")
                        .param("nom", "Dupont")
                        .param("dateNaissance", "1980-01-01")
                        .param("genre", "M"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().attributeHasFieldErrors("patient", "nom"));
    }

    @Test
    @WithMockUser
    void laModificationValideRedirigeVersLeDetail() throws Exception {
        PatientDTO updated = new PatientDTO();
        updated.setId(1L);
        when(patientGatewayClient.update(eq(1L), any(PatientDTO.class))).thenReturn(updated);

        mockMvc.perform(post("/patients/1")
                        .with(csrf())
                        .param("prenom", "Jean")
                        .param("nom", "Dupont")
                        .param("dateNaissance", "1980-01-01")
                        .param("genre", "M"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/1"));

        verify(patientGatewayClient).update(eq(1L), any(PatientDTO.class));
    }
}
