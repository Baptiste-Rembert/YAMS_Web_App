package com.yams.controller;

import com.yams.model.Game;
import com.yams.model.Player;
import com.yams.model.Turn;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScorecardInvalidScoreTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void invalidScoreRejected() throws Exception {
        String base = "http://localhost:" + port;
        HttpHeaders authHeaders = loginAndGetCookie(base, "bob");

        ResponseEntity<Game> createResp = restTemplate.postForEntity(base + "/api/games", null, Game.class);
        assertEquals(200, createResp.getStatusCodeValue());
        Long gameId = createResp.getBody().getId();

        HttpHeaders headers = jsonHeaders(authHeaders);
        String joinBody = "{\"username\":\"bob\"}";
        ResponseEntity<Player> joinResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/join", new HttpEntity<>(joinBody, headers), Player.class);
        assertEquals(200, joinResp.getStatusCodeValue());
        Long playerId = joinResp.getBody().getId();

        ResponseEntity<String> startResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/start", new HttpEntity<>(authHeaders), String.class);
        assertEquals(200, startResp.getStatusCodeValue());

        ResponseEntity<Turn> rollResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/turns/roll", new HttpEntity<>(authHeaders), Turn.class);
        assertEquals(200, rollResp.getStatusCodeValue());
        Turn turn = rollResp.getBody();
        String diceCsv = (turn == null) ? null : turn.getDice();

        int expectedOnes = 0;
        if (diceCsv != null && !diceCsv.isBlank()) {
            String[] parts = diceCsv.split(",");
            for (String p : parts) {
                int d = Integer.parseInt(p);
                if (d == 1) expectedOnes++;
            }
        }

        int invalidScore = expectedOnes + 1;
        String scoreBody = String.format("{\"category\":\"ONES\",\"score\":%d}", invalidScore);
        ResponseEntity<String> badResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/scorecard/" + playerId + "/score", new HttpEntity<>(scoreBody, headers), String.class);
        assertEquals(400, badResp.getStatusCodeValue());

        // forfeit allowed
        String forfeitBody = "{\"category\":\"ONES\",\"score\":0}";
        ResponseEntity<String> forfeitResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/scorecard/" + playerId + "/score", new HttpEntity<>(forfeitBody, headers), String.class);
        assertEquals(200, forfeitResp.getStatusCodeValue());

        // valid expected
        String validBody = String.format("{\"category\":\"ONES\",\"score\":%d}", expectedOnes);
        ResponseEntity<String> okResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/scorecard/" + playerId + "/score", new HttpEntity<>(validBody, headers), String.class);
        assertEquals(200, okResp.getStatusCodeValue());
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
