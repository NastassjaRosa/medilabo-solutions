package com.medilabo.notesservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabo.notesservice.dto.NoteRequestDTO;
import com.medilabo.notesservice.dto.NoteResponseDTO;
import com.medilabo.notesservice.exception.NoteNotFoundException;
import com.medilabo.notesservice.service.NoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests du contrôleur REST {@link NoteController} : le service métier est simulé
 * pour isoler la couche web (routage, validation, codes HTTP). Les filtres
 * Spring Security sont désactivés ici ; l'authentification est testée à part dans
 * {@link com.medilabo.notesservice.config.NoteSecurityTest}.
 */
@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteService noteService;

    private NoteResponseDTO sampleResponse() {
        return new NoteResponseDTO("64f1a2b3c4d5e6f7a8b9c0d1", 1L,
                "Le patient déclare qu'il se sent très bien.", Instant.parse("2026-01-01T09:00:00Z"));
    }

    private NoteRequestDTO validRequest() {
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setPatientId(1L);
        dto.setContenu("Le patient déclare qu'il se sent très bien.");
        return dto;
    }

    @Test
    void getByPatientId_shouldReturnListOfNotes() throws Exception {
        when(noteService.findByPatientId(1L)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/notes/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1));
    }

    @Test
    void getByPatientId_whenNoNotes_shouldReturnEmptyList() throws Exception {
        when(noteService.findByPatientId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/notes/patient/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_whenNoteExists_shouldReturnNote() throws Exception {
        when(noteService.findById("64f1a2b3c4d5e6f7a8b9c0d1")).thenReturn(sampleResponse());

        mockMvc.perform(get("/notes/64f1a2b3c4d5e6f7a8b9c0d1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1));
    }

    @Test
    void getById_whenNoteMissing_shouldReturn404() throws Exception {
        when(noteService.findById("inconnu")).thenThrow(new NoteNotFoundException("inconnu"));

        mockMvc.perform(get("/notes/inconnu"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withValidPayload_shouldReturn201() throws Exception {
        when(noteService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/notes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(1));
    }

    @Test
    void create_withBlankContenu_shouldReturn400() throws Exception {
        NoteRequestDTO invalid = validRequest();
        invalid.setContenu(" ");

        mockMvc.perform(post("/notes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withoutPatientId_shouldReturn400() throws Exception {
        NoteRequestDTO invalid = validRequest();
        invalid.setPatientId(null);

        mockMvc.perform(post("/notes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withContenuTooLong_shouldReturn400() throws Exception {
        NoteRequestDTO invalid = validRequest();
        invalid.setContenu("a".repeat(100_001));

        mockMvc.perform(post("/notes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withMultilineContenu_shouldPreserveLineBreaks() throws Exception {
        NoteRequestDTO multiline = validRequest();
        multiline.setContenu("Ligne 1\nLigne 2\nLigne 3");
        when(noteService.create(any())).thenReturn(
                new NoteResponseDTO("id", 1L, "Ligne 1\nLigne 2\nLigne 3", Instant.now()));

        mockMvc.perform(post("/notes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(multiline)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contenu").value("Ligne 1\nLigne 2\nLigne 3"));
    }
}
