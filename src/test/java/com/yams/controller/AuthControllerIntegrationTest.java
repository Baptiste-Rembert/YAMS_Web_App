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

        ResponseEntity<User> regResp = restTemplate.postForEntity(base + "/api/auth/register", entity, User.class);
        assertEquals(200, regResp.getStatusCodeValue());
        assertNotNull(regResp.getBody());
        assertEquals("itestuser", regResp.getBody().getUsername());
    }
}
