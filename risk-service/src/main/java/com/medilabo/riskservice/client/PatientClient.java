package com.medilabo.riskservice.client;

import com.medilabo.riskservice.client.dto.PatientDTO;
import com.medilabo.riskservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client REST déclaratif vers {@code patient-service}, appelé directement sur le
 * réseau Docker interne (pas via la gateway). L'URL cible est figée en
 * configuration ({@code risk.clients.patient-service.url}), jamais dérivée d'une
 * entrée utilisateur, pour exclure tout risque de SSRF.
 *
 * @since 1.0
 */
@FeignClient(name = "patient-service", url = "${risk.clients.patient-service.url}", configuration = FeignClientConfig.class)
public interface PatientClient {

    /**
     * Récupère les informations démographiques d'un patient.
     *
     * @param id identifiant du patient
     * @return le patient correspondant
     */
    @GetMapping("/patients/{id}")
    PatientDTO getById(@PathVariable("id") Long id);
}
