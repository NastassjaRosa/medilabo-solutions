package com.medilabo.riskservice.client;

import com.medilabo.riskservice.client.dto.NoteDTO;
import com.medilabo.riskservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Client REST déclaratif vers {@code notes-service}, appelé directement sur le
 * réseau Docker interne (pas via la gateway). L'URL cible est figée en
 * configuration ({@code risk.clients.notes-service.url}), jamais dérivée d'une
 * entrée utilisateur, pour exclure tout risque de SSRF.
 *
 * @since 1.0
 */
@FeignClient(name = "notes-service", url = "${risk.clients.notes-service.url}", configuration = FeignClientConfig.class)
public interface NotesClient {

    /**
     * Récupère les notes d'observation d'un patient.
     *
     * @param patientId identifiant du patient
     * @return les notes du patient, éventuellement une liste vide
     */
    @GetMapping("/notes/patient/{patientId}")
    List<NoteDTO> getByPatientId(@PathVariable("patientId") Long patientId);
}
