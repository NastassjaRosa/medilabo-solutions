package com.medilabo.riskservice.engine;

import com.medilabo.riskservice.client.dto.Genre;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Règles âge/genre du moteur de risque, indépendamment du texte des notes :
 * bornes de chaque palier pour les trois profils (plus de 30 ans, homme et
 * femme de moins de 30 ans).
 */
class RiskEvaluatorTest {

    private final RiskEvaluator evaluator = new RiskEvaluator();

    @Test
    void zeroOuUnDeclencheur_estToujoursNoneQuelQueSoitLageOuLeGenre() {
        assertThat(evaluator.evaluate(59, Genre.F, 0)).isEqualTo(RiskLevel.NONE);
        assertThat(evaluator.evaluate(20, Genre.M, 1)).isEqualTo(RiskLevel.NONE);
        assertThat(evaluator.evaluate(80, Genre.F, 1)).isEqualTo(RiskLevel.NONE);
    }

    @Test
    void plusDeTrenteAns_bornesBorderlineInDangerEarlyOnset() {
        assertThat(evaluator.evaluate(80, Genre.M, 2)).isEqualTo(RiskLevel.BORDERLINE);
        assertThat(evaluator.evaluate(80, Genre.M, 5)).isEqualTo(RiskLevel.BORDERLINE);
        assertThat(evaluator.evaluate(80, Genre.F, 6)).isEqualTo(RiskLevel.IN_DANGER);
        assertThat(evaluator.evaluate(80, Genre.F, 7)).isEqualTo(RiskLevel.IN_DANGER);
        assertThat(evaluator.evaluate(80, Genre.M, 8)).isEqualTo(RiskLevel.EARLY_ONSET);
        assertThat(evaluator.evaluate(80, Genre.M, 12)).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    @Test
    void moinsDeTrenteAns_homme_bornesInDangerEtEarlyOnset() {
        assertThat(evaluator.evaluate(20, Genre.M, 2)).isEqualTo(RiskLevel.NONE);
        assertThat(evaluator.evaluate(20, Genre.M, 3)).isEqualTo(RiskLevel.IN_DANGER);
        assertThat(evaluator.evaluate(20, Genre.M, 4)).isEqualTo(RiskLevel.IN_DANGER);
        assertThat(evaluator.evaluate(20, Genre.M, 5)).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    @Test
    void moinsDeTrenteAns_femme_bornesInDangerEtEarlyOnset() {
        assertThat(evaluator.evaluate(20, Genre.F, 3)).isEqualTo(RiskLevel.NONE);
        assertThat(evaluator.evaluate(20, Genre.F, 4)).isEqualTo(RiskLevel.IN_DANGER);
        assertThat(evaluator.evaluate(20, Genre.F, 6)).isEqualTo(RiskLevel.IN_DANGER);
        assertThat(evaluator.evaluate(20, Genre.F, 7)).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    @Test
    void ageExactementTrenteAns_estTraiteCommeMoinsDeTrenteAns_jamaisBorderline() {
        assertThat(evaluator.evaluate(30, Genre.M, 2)).isEqualTo(RiskLevel.NONE);
        assertThat(evaluator.evaluate(30, Genre.F, 3)).isEqualTo(RiskLevel.NONE);
    }
}
