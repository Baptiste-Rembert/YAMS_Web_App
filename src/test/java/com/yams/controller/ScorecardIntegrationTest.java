package com.yams.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yams.model.Game;
import com.yams.model.Player;
import com.yams.model.Scorecard;
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
public class ScorecardIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void updateAndGetScorecard() throws Exception {
        String base = "http://localhost:" + port;

        ResponseEntity<Game> createResp = restTemplate.postForEntity(base + "/api/games", null, Game.class);
        assertEquals(200, createResp.getStatusCodeValue());
        Long gameId = createResp.getBody().getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String joinBody = "{\"username\":\"carol\"}";
        ResponseEntity<Player> joinResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/join", new HttpEntity<>(joinBody, headers), Player.class);
        assertEquals(200, joinResp.getStatusCodeValue());
        Long playerId = joinResp.getBody().getId();

        ResponseEntity<String> startResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/start", null, String.class);
        assertEquals(200, startResp.getStatusCodeValue());

        String scoreBody = "{\"category\":\"ONES\",\"score\":5}";
        ResponseEntity<Scorecard> scoreResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/scorecard/" + playerId + "/score", new HttpEntity<>(scoreBody, headers), Scorecard.class);
        assertEquals(200, scoreResp.getStatusCodeValue());

        ResponseEntity<Scorecard> getResp = restTemplate.getForEntity(base + "/api/games/" + gameId + "/scorecard/" + playerId, Scorecard.class);
        assertEquals(200, getResp.getStatusCodeValue());
        Scorecard sc = getResp.getBody();
        assertNotNull(sc.getScores());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(sc.getScores());
        assertTrue(node.has("ONES"));
        assertEquals(5, node.get("ONES").asInt());

        // request computed summary
        ResponseEntity<String> summaryResp = restTemplate.getForEntity(base + "/api/games/" + gameId + "/scorecard/" + playerId + "/summary", String.class);
        assertEquals(200, summaryResp.getStatusCodeValue());
        JsonNode sumNode = mapper.readTree(summaryResp.getBody());
        assertEquals(5, sumNode.get("upperTotal").asInt());
        assertEquals(0, sumNode.get("upperBonus").asInt());
        assertEquals(0, sumNode.get("lowerTotal").asInt());
        assertEquals(5, sumNode.get("total").asInt());
    }
}
