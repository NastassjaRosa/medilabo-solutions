package com.medilabo.riskservice.engine;

import com.medilabo.riskservice.client.dto.Genre;
import org.springframework.stereotype.Component;

/**
 * Applique les règles de risque âge/genre du sujet OpenClassrooms à partir d'un
 * nombre de déclencheurs distincts déjà compté par
 * {@link com.medilabo.riskservice.trigger.TriggerDetector}. Seule responsabilité :
 * l'évaluation des règles (SRP) — le comptage des déclencheurs est une
 * responsabilité distincte, injectée ailleurs.
 *
 * <p><strong>Interprétation retenue pour les paliers "In Danger" côté patients de
 * moins de 30 ans.</strong> Le sujet énonce "trois déclencheurs" (homme) / "quatre
 * déclencheurs" (femme) pour In Danger, puis "au moins cinq" / "au moins sept" pour
 * Early Onset, sans couvrir explicitement les valeurs intermédiaires (ex. un homme
 * de moins de 30 ans avec 4 déclencheurs). Les seuils sont ici traités comme des
 * seuils <em>minimaux</em>, évalués du plus sévère au moins sévère : cela couvre
 * tous les comptages sans trou et fait toujours primer le niveau le plus grave.</p>
 *
 * @since 1.0
 */
@Component
public class RiskEvaluator {

    private static final int SEUIL_AGE = 30;

    /**
     * Évalue le niveau de risque de diabète d'un patient.
     *
     * @param age           âge du patient en années révolues
     * @param genre         genre administratif du patient
     * @param triggerCount  nombre de déclencheurs distincts trouvés dans ses notes
     * @return le niveau de risque calculé
     */
    public RiskLevel evaluate(int age, Genre genre, int triggerCount) {
        if (triggerCount <= 1) {
            return RiskLevel.NONE;
        }
        if (age > SEUIL_AGE) {
            return evaluatePlusDeTrente(triggerCount);
        }
        return genre == Genre.M ? evaluateHommeMoinsDeTrente(triggerCount) : evaluateFemmeMoinsDeTrente(triggerCount);
    }

    private RiskLevel evaluatePlusDeTrente(int triggerCount) {
        if (triggerCount >= 8) {
            return RiskLevel.EARLY_ONSET;
        }
        if (triggerCount >= 6) {
            return RiskLevel.IN_DANGER;
        }
        return RiskLevel.BORDERLINE;
    }

    private RiskLevel evaluateHommeMoinsDeTrente(int triggerCount) {
        if (triggerCount >= 5) {
            return RiskLevel.EARLY_ONSET;
        }
        if (triggerCount >= 3) {
            return RiskLevel.IN_DANGER;
        }
        return RiskLevel.NONE;
    }

    private RiskLevel evaluateFemmeMoinsDeTrente(int triggerCount) {
        if (triggerCount >= 7) {
            return RiskLevel.EARLY_ONSET;
        }
        if (triggerCount >= 4) {
            return RiskLevel.IN_DANGER;
        }
        return RiskLevel.NONE;
    }
}
