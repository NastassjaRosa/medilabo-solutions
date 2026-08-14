package com.medilabo.frontend.client;

import com.medilabo.frontend.dto.PatientDTO;
import com.medilabo.frontend.exception.GatewayUnavailableException;
import com.medilabo.frontend.exception.GatewayValidationException;
import com.medilabo.frontend.exception.PatientNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verifie que le client vers la gateway envoie bien l'en-tete HTTP Basic attendu, et qu'il
 * traduit les erreurs HTTP en exceptions metier explicites plutot que de les masquer.
 */
class PatientGatewayClientTest {

    private static final String USERNAME = "gw-user";
    private static final String PASSWORD = "gw-password";
    private static final String EXPECTED_AUTH_HEADER =
            "Basic " + Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());

    private MockRestServiceServer mockServer;
    private PatientGatewayClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://gateway-test:8080")
                .requestFactory(new SimpleClientHttpRequestFactory())
                .requestInterceptor(new BasicAuthenticationInterceptor(USERNAME, PASSWORD));
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new PatientGatewayClient(builder.build());
    }

    @Test
    void findAllEnvoieLenTeteBasicAuth() {
        mockServer.expect(requestTo("http://gateway-test:8080/patients"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", EXPECTED_AUTH_HEADER))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        List<PatientDTO> patients = client.findAll();

        assertThat(patients).isEmpty();
    }

    @Test
    void findByIdIntrouvableLeveUneExceptionDediee() {
        mockServer.expect(requestTo("http://gateway-test:8080/patients/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.findById(99L))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void createAvecErreurDeValidationPorteLesErreursParChamp() {
        String body = "{\"message\":\"Validation refusee\",\"fieldErrors\":{\"nom\":\"Le nom est obligatoire\"}}";
        mockServer.expect(requestTo("http://gateway-test:8080/patients"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body(body)
                        .contentType(MediaType.APPLICATION_JSON));

        PatientDTO patient = new PatientDTO();
        patient.setPrenom("Jean");

        assertThatThrownBy(() -> client.create(patient))
                .isInstanceOf(GatewayValidationException.class)
                .satisfies(e -> assertThat(((GatewayValidationException) e).getFieldErrors())
                        .containsEntry("nom", "Le nom est obligatoire"));
    }

    @Test
    void unePanneAmontNeRenvoieJamaisSilencieusementUneListeVide() {
        mockServer.expect(requestTo("http://gateway-test:8080/patients"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findAll())
                .isInstanceOf(GatewayUnavailableException.class);
    }
}
