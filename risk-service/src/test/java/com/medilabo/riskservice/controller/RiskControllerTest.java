package com.medilabo.riskservice.controller;

import com.medilabo.riskservice.engine.RiskLevel;
import com.medilabo.riskservice.exception.DependencyUnavailableException;
import com.medilabo.riskservice.exception.PatientNotFoundException;
import com.medilabo.riskservice.service.RiskAssessment;
import com.medilabo.riskservice.service.RiskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests du contrôleur REST {@link RiskController} : le service métier est simulé
 * pour isoler la couche web (routage, codes HTTP, format d'erreur). Les filtres
 * Spring Security sont désactivés ici ; l'authentification est testée à part dans
 * {@link com.medilabo.riskservice.config.RiskSecurityTest}.
 */
@WebMvcTest(RiskController.class)
@AutoConfigureMockMvc(addFilters = false)
class RiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiskService riskService;

    @Test
    void getRisk_patientExiste_renvoie200AvecLeNiveauDeRisque() throws Exception {
        when(riskService.evaluateRisk(1L)).thenReturn(new RiskAssessment(1L, "Test", "TestNone", RiskLevel.NONE));

        mockMvc.perform(get("/risk/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.riskLevel").value("None"));
    }

    @Test
    void getRisk_patientIntrouvable_renvoie404() throws Exception {
        when(riskService.evaluateRisk(99L)).thenThrow(new PatientNotFoundException(99L));

        mockMvc.perform(get("/risk/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRisk_dependanceIndisponible_renvoie503_jamaisUnNiveauNone() throws Exception {
        when(riskService.evaluateRisk(1L))
                .thenThrow(new DependencyUnavailableException("notes-service", new RuntimeException("timeout")));

        mockMvc.perform(get("/risk/1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("Risque indéterminé : notes-service indisponible"));
    }
}
