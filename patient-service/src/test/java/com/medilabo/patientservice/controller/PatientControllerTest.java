package com.medilabo.patientservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabo.patientservice.dto.PatientRequestDTO;
import com.medilabo.patientservice.dto.PatientResponseDTO;
import com.medilabo.patientservice.entity.Genre;
import com.medilabo.patientservice.exception.PatientNotFoundException;
import com.medilabo.patientservice.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests du contrôleur REST {@link PatientController} : le service métier est
 * simulé pour isoler la couche web (routage, validation, codes HTTP). Les filtres
 * Spring Security sont désactivés ici ; l'authentification est testée à part dans
 * {@link com.medilabo.patientservice.config.PatientSecurityTest}.
 */
@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    private PatientResponseDTO sampleResponse() {
        return new PatientResponseDTO(1L, "Test", "TestNone", LocalDate.of(1966, 12, 31),
                Genre.F, "1 Brookside St", "100-222-3333");
    }

    private PatientRequestDTO validRequest() {
        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setPrenom("Test");
        dto.setNom("TestNone");
        dto.setDateNaissance(LocalDate.of(1966, 12, 31));
        dto.setGenre(Genre.F);
        dto.setAdresse("1 Brookside St");
        dto.setTelephone("100-222-3333");
        return dto;
    }

    @Test
    void getAll_shouldReturnListOfPatients() throws Exception {
        when(patientService.findAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("TestNone"));
    }

    @Test
    void getById_whenPatientExists_shouldReturnPatient() throws Exception {
        when(patientService.findById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenom").value("Test"));
    }

    @Test
    void getById_whenPatientMissing_shouldReturn404() throws Exception {
        when(patientService.findById(99L)).thenThrow(new PatientNotFoundException(99L));

        mockMvc.perform(get("/patients/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withValidPayload_shouldReturn201() throws Exception {
        when(patientService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/patients")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("TestNone"));
    }

    @Test
    void create_withBlankPrenom_shouldReturn400() throws Exception {
        PatientRequestDTO invalid = validRequest();
        invalid.setPrenom(" ");

        mockMvc.perform(post("/patients")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withoutOptionalFields_shouldReturn201() throws Exception {
        PatientRequestDTO withoutOptional = validRequest();
        withoutOptional.setAdresse(null);
        withoutOptional.setTelephone(null);
        when(patientService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/patients")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(withoutOptional)))
                .andExpect(status().isCreated());
    }

    @Test
    void update_whenPatientExists_shouldReturn200() throws Exception {
        when(patientService.update(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(put("/patients/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("TestNone"));
    }

    @Test
    void update_whenPatientMissing_shouldReturn404() throws Exception {
        when(patientService.update(eq(99L), any())).thenThrow(new PatientNotFoundException(99L));

        mockMvc.perform(put("/patients/99")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }
}
