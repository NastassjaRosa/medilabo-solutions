package com.medilabo.riskservice.controller;

import com.medilabo.riskservice.dto.RiskResponseDTO;
import com.medilabo.riskservice.service.RiskAssessment;
import com.medilabo.riskservice.service.RiskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expose le niveau de risque de diabète de type 2 d'un patient.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/risk")
public class RiskController {

    private final RiskService riskService;

    /**
     * @param riskService évaluation métier du risque
     */
    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    /**
     * Consulte le risque de diabète d'un patient.
     *
     * @param patientId identifiant du patient
     * @return le niveau de risque calculé
     */
    @GetMapping("/{patientId}")
    public RiskResponseDTO getRisk(@PathVariable Long patientId) {
        RiskAssessment assessment = riskService.evaluateRisk(patientId);
        return new RiskResponseDTO(assessment.patientId(), assessment.prenom(), assessment.nom(), assessment.riskLevel());
    }
}
