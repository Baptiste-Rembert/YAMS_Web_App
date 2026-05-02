package com.yams.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yams.model.GameEvent;
import com.yams.model.GameEventLog;
import com.yams.repository.GameEventLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameEventService {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameEventLogRepository gameEventLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GameEventService(SimpMessagingTemplate messagingTemplate, GameEventLogRepository gameEventLogRepository) {
        this.messagingTemplate = messagingTemplate;
        this.gameEventLogRepository = gameEventLogRepository;
    }

    @Transactional
    public void sendEvent(Long gameId, GameEvent event) {
        saveHistory(event);
        String dest = "/topic/game." + gameId + ".events";
        messagingTemplate.convertAndSend(dest, event);
    }

    public void sendEvent(Long gameId, String type, Map<String, Object> data) {
        sendEvent(gameId, new GameEvent(type, gameId, data));
    }

    public List<GameEventHistoryEntry> listHistory(Long gameId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return gameEventLogRepository.findByGameIdOrderByIdDesc(gameId, PageRequest.of(0, safeLimit))
                .getContent()
                .stream()
                .map(this::toHistoryEntry)
                .toList();
    }

    private void saveHistory(GameEvent event) {
        try {
            GameEventLog log = new GameEventLog();
            log.setGameId(event.getGameId());
            log.setEventType(event.getType());
            log.setPayloadJson(objectMapper.writeValueAsString(event.getData() == null ? Map.of() : event.getData()));
            log.setCreatedAt(Instant.now());
            gameEventLogRepository.save(log);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to persist game event history", e);
        }
    }

    private GameEventHistoryEntry toHistoryEntry(GameEventLog log) {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            data = objectMapper.readValue(log.getPayloadJson(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // keep empty payload if malformed
        }
        return new GameEventHistoryEntry(log.getId(), log.getEventType(), log.getGameId(), data, log.getCreatedAt());
    }
}
