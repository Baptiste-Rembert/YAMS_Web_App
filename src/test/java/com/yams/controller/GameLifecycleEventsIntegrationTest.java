package com.yams.controller;

import com.yams.model.Game;
import com.yams.model.GameEvent;
import com.yams.model.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameLifecycleEventsIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void joinAndStartBroadcastLifecycleEvents() throws Exception {
        String base = "http://localhost:" + port;
        HttpHeaders authHeaders = loginAndGetCookie(base, "lifecycle-user");

        Game game = restTemplate.postForEntity(base + "/api/games", null, Game.class).getBody();
        assertNotNull(game);

        String url = "ws://localhost:" + port + "/ws";
        WebSocketStompClient stompClient = new WebSocketStompClient(
                new SockJsClient(Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient())))
        );
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        BlockingQueue<GameEvent> queue = new LinkedBlockingDeque<>();
        StompSession session = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
        session.subscribe("/topic/game." + game.getId() + ".events", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add((GameEvent) payload);
            }
        });

        String joinBody = "{\"username\":\"lifecycle-user\"}";
        restTemplate.postForEntity(base + "/api/games/" + game.getId() + "/join", new HttpEntity<>(joinBody, jsonHeaders(authHeaders)), Player.class);
        GameEvent joined = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(joined);
        assertEquals("PLAYER_JOINED", joined.getType());

        restTemplate.postForEntity(base + "/api/games/" + game.getId() + "/start", new HttpEntity<>(authHeaders), String.class);
        GameEvent started = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(started);
        assertEquals("GAME_STARTED", started.getType());

        session.disconnect();
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