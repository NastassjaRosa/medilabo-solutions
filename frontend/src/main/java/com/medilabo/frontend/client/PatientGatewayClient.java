package com.medilabo.frontend.client;

import com.medilabo.frontend.dto.PatientDTO;
import com.medilabo.frontend.exception.GatewayUnavailableException;
import com.medilabo.frontend.exception.GatewayValidationException;
import com.medilabo.frontend.exception.PatientNotFoundException;
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
 * Appelle le dossier demographique patient expose par le patient-service, via la gateway
 * (chaine API HTTP Basic). Traduit les erreurs HTTP en exceptions metier explicites : ne
 * masque jamais une panne aval par une reponse vide (voir A10 - pas de faux "aucun patient").
 *
 * @since 1.0
 */
@Component
public class PatientGatewayClient {

    private static final String PATIENTS_PATH = "/patients";

    private final RestClient restClient;

    /**
     * @param restClient client REST configure vers la gateway (voir GatewayClientConfig)
     */
    public PatientGatewayClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Liste tous les patients.
     *
     * @return la liste des patients
     */
    public List<PatientDTO> findAll() {
        return execute(() -> restClient.get()
                .uri(PATIENTS_PATH)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PatientDTO>>() {
                }));
    }

    /**
     * Recupere un patient par identifiant.
     *
     * @param id identifiant du patient
     * @return le patient correspondant
     * @throws PatientNotFoundException si aucun patient ne correspond a cet identifiant
     */
    public PatientDTO findById(Long id) {
        try {
            return execute(() -> restClient.get()
                    .uri(PATIENTS_PATH + "/{id}", id)
                    .retrieve()
                    .body(PatientDTO.class));
        } catch (HttpClientErrorException.NotFound notFound) {
            throw new PatientNotFoundException(id);
        }
    }

    /**
     * Cree un patient.
     *
     * @param patient donnees du patient a creer
     * @return le patient cree (avec son identifiant)
     * @throws GatewayValidationException si le patient-service rejette les donnees saisies
     */
    public PatientDTO create(PatientDTO patient) {
        try {
            return execute(() -> restClient.post()
                    .uri(PATIENTS_PATH)
                    .body(patient)
                    .retrieve()
                    .body(PatientDTO.class));
        } catch (HttpClientErrorException.BadRequest badRequest) {
            throw toValidationException(badRequest);
        }
    }

    /**
     * Met a jour un patient existant.
     *
     * @param id      identifiant du patient a mettre a jour
     * @param patient nouvelles donnees du patient
     * @return le patient mis a jour
     * @throws PatientNotFoundException  si aucun patient ne correspond a cet identifiant
     * @throws GatewayValidationException si le patient-service rejette les donnees saisies
     */
    public PatientDTO update(Long id, PatientDTO patient) {
        try {
            return execute(() -> restClient.put()
                    .uri(PATIENTS_PATH + "/{id}", id)
                    .body(patient)
                    .retrieve()
                    .body(PatientDTO.class));
        } catch (HttpClientErrorException.NotFound notFound) {
            throw new PatientNotFoundException(id);
        } catch (HttpClientErrorException.BadRequest badRequest) {
            throw toValidationException(badRequest);
        }
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.BadRequest e) {
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
}
