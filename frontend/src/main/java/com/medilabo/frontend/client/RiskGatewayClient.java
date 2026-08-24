package com.medilabo.frontend.client;

import com.medilabo.frontend.dto.RiskDTO;
import com.medilabo.frontend.exception.GatewayUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

/**
 * Appelle l'evaluation du risque de diabete exposee par le risk-service, via la gateway
 * (chaine API HTTP Basic), comme {@link PatientGatewayClient} et {@link NoteGatewayClient}.
 *
 * @since 1.0
 */
@Component
public class RiskGatewayClient {

    private static final String RISK_PATH = "/risk";

    private final RestClient restClient;

    /**
     * @param restClient client REST configure vers la gateway (voir GatewayClientConfig)
     */
    public RiskGatewayClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Recupere le niveau de risque de diabete d'un patient.
     *
     * @param patientId identifiant du patient
     * @return le risque evalue
     * @throws GatewayUnavailableException si le risk-service ou une de ses dependances est
     *                                      indisponible : le risque est alors indetermine, et
     *                                      ne doit jamais etre presente comme "None" par defaut
     */
    public RiskDTO findByPatientId(Long patientId) {
        return execute(() -> restClient.get()
                .uri(RISK_PATH + "/{patientId}", patientId)
                .retrieve()
                .body(RiskDTO.class));
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientException e) {
            throw new GatewayUnavailableException("Appel a la gateway indisponible ou en echec", e);
        }
    }
}
