package com.medilabo.frontend.client;

import com.medilabo.frontend.dto.NoteDTO;
import com.medilabo.frontend.dto.NoteFormDTO;
import com.medilabo.frontend.exception.GatewayUnavailableException;
import com.medilabo.frontend.exception.GatewayValidationException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Appelle les notes d'observation medicale exposees par le notes-service, via la gateway
 * (chaine API HTTP Basic). Traduit les erreurs HTTP en exceptions metier explicites, comme
 * {@link PatientGatewayClient}.
 *
 * @since 1.0
 */
@Component
public class NoteGatewayClient {

    private static final String NOTES_PATH = "/notes";

    private final RestClient restClient;

    /**
     * @param restClient client REST configure vers la gateway (voir GatewayClientConfig)
     */
    public NoteGatewayClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Liste l'historique des notes d'un patient, triees par date de redaction.
     *
     * @param patientId identifiant du patient
     * @return les notes du patient
     */
    public List<NoteDTO> findByPatientId(Long patientId) {
        return execute(() -> restClient.get()
                .uri(NOTES_PATH + "/patient/{patientId}", patientId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<NoteDTO>>() {
                }));
    }

    /**
     * Ajoute une note d'observation pour un patient.
     *
     * @param patientId identifiant du patient concerne, pris depuis l'URL cote controleur
     * @param noteForm  contenu saisi par le praticien
     * @return la note creee
     * @throws GatewayValidationException si le notes-service rejette les donnees saisies
     */
    public NoteDTO create(Long patientId, NoteFormDTO noteForm) {
        try {
            return execute(() -> restClient.post()
                    .uri(NOTES_PATH)
                    .body(new NoteCreateRequest(patientId, noteForm.getContenu()))
                    .retrieve()
                    .body(NoteDTO.class));
        } catch (HttpClientErrorException.BadRequest badRequest) {
            throw toValidationException(badRequest);
        }
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpClientErrorException.BadRequest e) {
            throw e;
        } catch (RestClientException e) {
            throw new GatewayUnavailableException("Appel a la gateway indisponible ou en echec", e);
        }
    }

    private GatewayValidationException toValidationException(HttpClientErrorException.BadRequest badRequest) {
        try {
            GatewayErrorBody body = badRequest.getResponseBodyAs(GatewayErrorBody.class);
            Map<String, String> fieldErrors = (body != null && body.fieldErrors() != null)
                    ? body.fieldErrors()
                    : Collections.emptyMap();
            return new GatewayValidationException(fieldErrors);
        } catch (RestClientException parsingFailure) {
            return new GatewayValidationException(Collections.emptyMap());
        }
    }

    private record GatewayErrorBody(String message, Map<String, String> fieldErrors) {
    }

    private record NoteCreateRequest(Long patientId, String contenu) {
    }
}
