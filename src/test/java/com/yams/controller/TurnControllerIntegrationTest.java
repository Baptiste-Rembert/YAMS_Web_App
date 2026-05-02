package com.yams.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yams.model.Game;
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
public class TurnControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void rollAndEndTurnFlow() throws Exception {
        String base = "http://localhost:" + port;

        ResponseEntity<Game> createResp = restTemplate.postForEntity(base + "/api/games", null, Game.class);
        assertEquals(200, createResp.getStatusCodeValue());
        Long gameId = createResp.getBody().getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String joinBody1 = "{\"username\":\"alice\"}";
        String joinBody2 = "{\"username\":\"bob\"}";
        restTemplate.postForEntity(base + "/api/games/" + gameId + "/join", new HttpEntity<>(joinBody1, headers), String.class);
        restTemplate.postForEntity(base + "/api/games/" + gameId + "/join", new HttpEntity<>(joinBody2, headers), String.class);

        ResponseEntity<String> startResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/start", null, String.class);
        assertEquals(200, startResp.getStatusCodeValue());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode startNode = mapper.readTree(startResp.getBody());
        Long currentPlayerBefore = startNode.get("currentPlayerId").asLong();

        ResponseEntity<String> rollResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/turns/roll", null, String.class);
        assertEquals(200, rollResp.getStatusCodeValue());
        JsonNode rollNode = mapper.readTree(rollResp.getBody());
        assertTrue(rollNode.has("dice"));
        String dice = rollNode.get("dice").asText();
        String[] parts = dice.split(",");
        assertEquals(5, parts.length);
        int rolls = rollNode.get("rolls").asInt();
        assertEquals(1, rolls);

        // perform a selective reroll for indices 0 and 2
        String rerollBody = "{\"indices\":[0,2]}";
        ResponseEntity<String> rerollResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/turns/reroll", new HttpEntity<>(rerollBody, headers), String.class);
        assertEquals(200, rerollResp.getStatusCodeValue());
        JsonNode rerollNode = mapper.readTree(rerollResp.getBody());
        assertTrue(rerollNode.has("dice"));
        String diceAfter = rerollNode.get("dice").asText();
        String[] partsAfter = diceAfter.split(",");
        assertEquals(5, partsAfter.length);
        int rollsAfter = rerollNode.get("rolls").asInt();
        assertEquals(2, rollsAfter);
        // ensure at least one selected index changed
        boolean changed = !parts[0].equals(partsAfter[0]) || !parts[2].equals(partsAfter[2]);
        assertTrue(changed);

        ResponseEntity<String> endResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/turns/end", null, String.class);
        assertEquals(200, endResp.getStatusCodeValue());
        JsonNode endNode = mapper.readTree(endResp.getBody());
        Long currentPlayerAfter = endNode.get("currentPlayerId").asLong();
        assertNotEquals(currentPlayerBefore, currentPlayerAfter);
    }
}
