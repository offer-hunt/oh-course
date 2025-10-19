package ru.offer.hunt.oh_course.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import com.nimbusds.jose.jwk.RSAKey;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
                "AUTH_ISSUER=http://localhost:8999",
                "AUTH_AUDIENCE=oh-course",
                "AUTH_JWKS_URL=http://localhost:8999/.well-known/jwks.json",
                "auth.issuer=http://localhost:8999",
                "auth.audience=oh-course",
                "auth.jwks-url=http://localhost:8999/.well-known/jwks.json"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityTest {

    private static WireMockServer wireMock;
    private static RSAKey rsaKey;

    @Value("${auth.issuer}")
    private String issuer;

    @Value("${auth.audience}")
    private String audience;

    @Autowired private MockMvc mvc;

    @BeforeAll
    static void setUp() {
        wireMock = new WireMockServer(8999);
        wireMock.start();
        WireMock.configureFor("localhost", 8999);
        rsaKey = JwksTestUtil.generateRsa("kid-test");
        WireMock.stubFor(
                WireMock.get("/.well-known/jwks.json")
                        .willReturn(WireMock.okJson(JwksTestUtil.toJwksJson(rsaKey))));
    }

    @AfterAll
    static void tearDown() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @Test
    void ok200() throws Exception {
        String token =
                JwtTestUtil.rs256Token(rsaKey, issuer, audience, List.of("USER"), List.of("course.read"));
        mvc.perform(get("/api/secure/ping").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void unauthorized401() throws Exception {
        mvc.perform(get("/api/ping")).andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden403() throws Exception {
        String token = JwtTestUtil.rs256Token(rsaKey, issuer, audience, List.of("USER"), List.of());
        mvc.perform(get("/api/secure/ping").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
