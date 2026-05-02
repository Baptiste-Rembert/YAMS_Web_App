package com.yams.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void ping() {
        String base = "http://localhost:" + port;
        ResponseEntity<String> resp = restTemplate.getForEntity(base + "/api/chat/ping", String.class);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("chat ok", resp.getBody());
    }
}
