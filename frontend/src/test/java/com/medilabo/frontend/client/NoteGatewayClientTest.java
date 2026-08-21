package com.medilabo.frontend.client;

import com.medilabo.frontend.dto.NoteDTO;
import com.medilabo.frontend.dto.NoteFormDTO;
import com.medilabo.frontend.exception.GatewayUnavailableException;
import com.medilabo.frontend.exception.GatewayValidationException;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verifie que le client vers la gateway envoie bien l'en-tete HTTP Basic attendu, transmet le
 * patientId pris cote serveur, et traduit les erreurs HTTP en exceptions metier explicites.
 */
class NoteGatewayClientTest {

    private static final String USERNAME = "gw-user";
    private static final String PASSWORD = "gw-password";
    private static final String EXPECTED_AUTH_HEADER =
            "Basic " + Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());

    private MockRestServiceServer mockServer;
    private NoteGatewayClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://gateway-test:8080")
                .requestFactory(new SimpleClientHttpRequestFactory())
                .requestInterceptor(new BasicAuthenticationInterceptor(USERNAME, PASSWORD));
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new NoteGatewayClient(builder.build());
    }

    @Test
    void findByPatientIdEnvoieLenTeteBasicAuth() {
        mockServer.expect(requestTo("http://gateway-test:8080/notes/patient/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", EXPECTED_AUTH_HEADER))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        List<NoteDTO> notes = client.findByPatientId(1L);

        assertThat(notes).isEmpty();
    }

    @Test
    void createEnvoieLePatientIdPrisCoteServeurEtLeContenuSaisi() {
        mockServer.expect(requestTo("http://gateway-test:8080/notes"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", EXPECTED_AUTH_HEADER))
                .andExpect(content().json("{\"patientId\":1,\"contenu\":\"Le patient va bien.\"}"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .body("{\"id\":\"abc\",\"patientId\":1,\"contenu\":\"Le patient va bien.\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        NoteFormDTO form = new NoteFormDTO();
        form.setContenu("Le patient va bien.");

        NoteDTO created = client.create(1L, form);

        assertThat(created.getId()).isEqualTo("abc");
        assertThat(created.getContenu()).isEqualTo("Le patient va bien.");
    }

    @Test
    void createAvecErreurDeValidationPorteLesErreursParChamp() {
        String body = "{\"message\":\"Donnees invalides\",\"fieldErrors\":{\"contenu\":\"Le contenu de la note est obligatoire\"}}";
        mockServer.expect(requestTo("http://gateway-test:8080/notes"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body(body)
                        .contentType(MediaType.APPLICATION_JSON));

        NoteFormDTO form = new NoteFormDTO();

        assertThatThrownBy(() -> client.create(1L, form))
                .isInstanceOf(GatewayValidationException.class)
                .satisfies(e -> assertThat(((GatewayValidationException) e).getFieldErrors())
                        .containsEntry("contenu", "Le contenu de la note est obligatoire"));
    }

    @Test
    void unePanneAmontLorsDeLaRecuperationNeRenvoieJamaisSilencieusementUneListeVide() {
        mockServer.expect(requestTo("http://gateway-test:8080/notes/patient/1"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findByPatientId(1L))
                .isInstanceOf(GatewayUnavailableException.class);
    }
}
