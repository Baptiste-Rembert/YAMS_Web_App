package com.yams.service;

import java.time.Instant;
import java.util.Map;

public record GameEventHistoryEntry(Long id, String type, Long gameId, Map<String, Object> data, Instant createdAt) {
}