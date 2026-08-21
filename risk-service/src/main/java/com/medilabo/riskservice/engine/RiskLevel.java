package com.medilabo.riskservice.engine;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Niveau de risque de diabète de type 2, tel que défini par le sujet
 * OpenClassrooms. Le libellé JSON reproduit exactement la terminologie attendue
 * ({@code "None"}, {@code "Borderline"}, {@code "In Danger"}, {@code "Early onset"}).
 *
 * @since 1.0
 */
public enum RiskLevel {

    /** Aucune note contenant de terme déclencheur (0 ou 1 déclencheur). */
    NONE("None"),
    /** Entre deux et cinq déclencheurs, patient de plus de 30 ans. */
    BORDERLINE("Borderline"),
    /** Trois à sept déclencheurs selon l'âge et le genre, voir {@link RiskEvaluator}. */
    IN_DANGER("In Danger"),
    /** Cinq déclencheurs ou plus selon l'âge et le genre, voir {@link RiskEvaluator}. */
    EARLY_ONSET("Early onset");

    private final String label;

    RiskLevel(String label) {
        this.label = label;
    }

    /**
     * @return le libellé attendu par le sujet, utilisé pour la sérialisation JSON
     */
    @JsonValue
    public String getLabel() {
        return label;
    }
}
