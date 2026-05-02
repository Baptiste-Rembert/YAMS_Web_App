package com.yams.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yams.model.Game;
import com.yams.model.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameEventHistoryIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void historyEndpointReturnsStoredEvents() throws Exception {
        String base = "http://localhost:" + port;
        HttpHeaders authHeaders = loginAndGetCookie(base, "history-user");

        ResponseEntity<Game> createResp = restTemplate.postForEntity(base + "/api/games", null, Game.class);
        assertEquals(200, createResp.getStatusCodeValue());
        Long gameId = createResp.getBody().getId();

        HttpHeaders jsonHeaders = jsonHeaders(authHeaders);
        String joinBody = "{\"username\":\"history-user\"}";
        ResponseEntity<Player> joinResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/join", new HttpEntity<>(joinBody, jsonHeaders), Player.class);
        assertEquals(200, joinResp.getStatusCodeValue());

        restTemplate.postForEntity(base + "/api/games/" + gameId + "/start", new HttpEntity<>(authHeaders), String.class);
        restTemplate.postForEntity(base + "/api/games/" + gameId + "/turns/roll", new HttpEntity<>(authHeaders), String.class);

        ResponseEntity<String> historyResp = restTemplate.getForEntity(base + "/api/games/" + gameId + "/events?limit=5", String.class);
        assertEquals(200, historyResp.getStatusCodeValue());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(historyResp.getBody());
        assertTrue(root.isArray());
        assertNotNull(root.get(0));
        assertTrue(root.size() > 0);
        assertEquals("TURN_ROLLED", root.get(0).path("type").asText());
        assertTrue(historyResp.getBody().contains("TURN_ROLLED"));
        assertTrue(historyResp.getBody().contains("GAME_STARTED"));
        assertTrue(historyResp.getBody().contains("PLAYER_JOINED"));
        assertTrue(historyResp.getBody().contains("GAME_CREATED"));
    }

    private HttpHeaders loginAndGetCookie(String base, String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> loginResp = restTemplate.postForEntity(base + "/api/auth/login", new HttpEntity<>(("{\"username\":\"" + username + "\"}"), headers), String.class);
        assertEquals(200, loginResp.getStatusCodeValue());
        HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.add(HttpHeaders.COOKIE, extractSessionCookie(loginResp.getHeaders().getFirst(HttpHeaders.SET_COOKIE)));
        return cookieHeaders;
    }

    private HttpHeaders jsonHeaders(HttpHeaders cookieHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.putAll(cookieHeaders);
        return headers;
    }

    private String extractSessionCookie(String setCookieHeader) {
        if (setCookieHeader == null || setCookieHeader.isBlank()) return null;
        int separator = setCookieHeader.indexOf(';');
        return separator >= 0 ? setCookieHeader.substring(0, separator) : setCookieHeader;
    }
}