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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createListJoinGetFlow() throws Exception {
        String base = "http://localhost:" + port;

        ResponseEntity<Game> createResp = restTemplate.postForEntity(base + "/api/games", null, Game.class);
        assertEquals(200, createResp.getStatusCodeValue());
        assertNotNull(createResp.getBody());
        Long gameId = createResp.getBody().getId();
        assertNotNull(gameId);

        ResponseEntity<Game[]> listResp = restTemplate.getForEntity(base + "/api/games", Game[].class);
        assertEquals(200, listResp.getStatusCodeValue());
        assertTrue(listResp.getBody().length >= 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String joinBody = "{\"username\":\"player1\"}";
        HttpEntity<String> joinEntity = new HttpEntity<>(joinBody, headers);
        ResponseEntity<Player> joinResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/join", joinEntity, Player.class);
        assertEquals(200, joinResp.getStatusCodeValue());
        assertNotNull(joinResp.getBody());
        assertEquals(gameId, joinResp.getBody().getGameId());

        // start the game
        ResponseEntity<String> startResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/start", null, String.class);
        assertEquals(200, startResp.getStatusCodeValue());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode startNode = mapper.readTree(startResp.getBody());
        assertTrue(startNode.get("started").asBoolean());
        assertTrue(startNode.has("currentPlayerId") && !startNode.get("currentPlayerId").isNull());

        ResponseEntity<String> detailResp = restTemplate.getForEntity(base + "/api/games/" + gameId, String.class);
        assertEquals(200, detailResp.getStatusCodeValue());
        JsonNode node = mapper.readTree(detailResp.getBody());
        assertEquals(gameId.longValue(), node.get("id").asLong());
        JsonNode playersNode = node.get("players");
        assertTrue(playersNode.isArray() && playersNode.size() >= 1);
    }
}
