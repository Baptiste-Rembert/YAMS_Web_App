package com.yams.controller;

import com.yams.model.GameEvent;
import com.yams.model.Game;
import com.yams.model.Player;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameEventWebSocketIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void receiveRollEvent() throws Exception {
        String base = "http://localhost:" + port;

        ResponseEntity<Game> createResp = restTemplate.postForEntity(base + "/api/games", null, Game.class);
        assertEquals(200, createResp.getStatusCodeValue());
        Long gameId = createResp.getBody().getId();

        String joinBody = "{\"username\":\"dave\"}";
        ResponseEntity<Player> joinResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/join", new org.springframework.http.HttpEntity<>(joinBody, createJsonHeaders()), Player.class);
        assertEquals(200, joinResp.getStatusCodeValue());

        ResponseEntity<String> startResp = restTemplate.postForEntity(base + "/api/games/" + gameId + "/start", null, String.class);
        assertEquals(200, startResp.getStatusCodeValue());

        String url = "ws://localhost:" + port + "/ws";

        WebSocketStompClient stompClient = new WebSocketStompClient(
                new SockJsClient(Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient())))
        );
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        BlockingQueue<GameEvent> blockingQueue = new LinkedBlockingDeque<>();

        StompSession session = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);

        session.subscribe("/topic/game." + gameId + ".events", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((GameEvent) payload);
            }
        });

        // trigger a roll which should send an event
        restTemplate.postForEntity(base + "/api/games/" + gameId + "/turns/roll", null, String.class);

        GameEvent evt = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(evt);
        assertEquals("ROLLED", evt.getType());
        assertEquals(gameId, evt.getGameId());
        assertTrue(evt.getData().containsKey("dice"));

        session.disconnect();
    }

    private org.springframework.http.HttpHeaders createJsonHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return headers;
    }
}
