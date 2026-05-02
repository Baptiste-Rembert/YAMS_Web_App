package com.yams.controller;

import com.yams.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void loginAndRegister() {
        String base = "http://localhost:" + port;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"username\":\"itestuser\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<User> loginResp = restTemplate.postForEntity(base + "/api/auth/login", entity, User.class);
        assertEquals(200, loginResp.getStatusCodeValue());
        assertNotNull(loginResp.getBody());
        assertEquals("itestuser", loginResp.getBody().getUsername());
        String sessionCookie = extractSessionCookie(loginResp.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        assertNotNull(sessionCookie);

        HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.add(HttpHeaders.COOKIE, sessionCookie);
        ResponseEntity<User> meResp = restTemplate.exchange(base + "/api/auth/me", org.springframework.http.HttpMethod.GET, new HttpEntity<>(cookieHeaders), User.class);
        assertEquals(200, meResp.getStatusCodeValue());
        assertEquals("itestuser", meResp.getBody().getUsername());

        ResponseEntity<User> regResp = restTemplate.postForEntity(base + "/api/auth/register", entity, User.class);
        assertEquals(200, regResp.getStatusCodeValue());
        assertNotNull(regResp.getBody());
        assertEquals("itestuser", regResp.getBody().getUsername());

        ResponseEntity<Void> logoutResp = restTemplate.postForEntity(base + "/api/auth/logout", new HttpEntity<>(cookieHeaders), Void.class);
        assertEquals(204, logoutResp.getStatusCodeValue());

        ResponseEntity<User> meAfterLogout = restTemplate.exchange(base + "/api/auth/me", org.springframework.http.HttpMethod.GET, new HttpEntity<>(cookieHeaders), User.class);
        assertEquals(401, meAfterLogout.getStatusCodeValue());
    }

    private String extractSessionCookie(String setCookieHeader) {
        if (setCookieHeader == null || setCookieHeader.isBlank()) {
            return null;
        }
        int separator = setCookieHeader.indexOf(';');
        return separator >= 0 ? setCookieHeader.substring(0, separator) : setCookieHeader;
    }
}
