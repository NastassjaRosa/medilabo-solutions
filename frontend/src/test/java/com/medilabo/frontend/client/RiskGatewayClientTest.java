package com.medilabo.frontend.client;

import com.medilabo.frontend.dto.RiskDTO;
import com.medilabo.frontend.exception.GatewayUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verifie que le client vers la gateway envoie bien l'en-tete HTTP Basic attendu et traduit
 * une panne aval (risk-service ou l'une de ses dependances indisponible) en
 * {@link GatewayUnavailableException} explicite, jamais en risque "None" par defaut.
 */
class RiskGatewayClientTest {

    private static final String USERNAME = "gw-user";
    private static final String PASSWORD = "gw-password";
    private static final String EXPECTED_AUTH_HEADER =
            "Basic " + Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());

    private MockRestServiceServer mockServer;
    private RiskGatewayClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://gateway-test:8080")
                .requestFactory(new SimpleClientHttpRequestFactory())
                .requestInterceptor(new BasicAuthenticationInterceptor(USERNAME, PASSWORD));
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new RiskGatewayClient(builder.build());
    }

    @Test
    void findByPatientIdEnvoieLenTeteBasicAuthEtRenvoieLeRisque() {
        mockServer.expect(requestTo("http://gateway-test:8080/risk/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", EXPECTED_AUTH_HEADER))
                .andRespond(withSuccess(
                        "{\"patientId\":1,\"prenom\":\"Jean\",\"nom\":\"Dupont\",\"riskLevel\":\"Borderline\"}",
                        MediaType.APPLICATION_JSON));

        RiskDTO risk = client.findByPatientId(1L);

        assertThat(risk.getPatientId()).isEqualTo(1L);
        assertThat(risk.getRiskLevel()).isEqualTo("Borderline");
    }

    @Test
    void unePanneAmontNeRenvoieJamaisSilencieusementUnRisqueParDefaut() {
        mockServer.expect(requestTo("http://gateway-test:8080/risk/1"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findByPatientId(1L))
                .isInstanceOf(GatewayUnavailableException.class);
    }
}
