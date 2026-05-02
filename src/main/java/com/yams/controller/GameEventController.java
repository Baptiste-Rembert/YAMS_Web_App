package com.yams.controller;

import com.yams.service.GameEventHistoryEntry;
import com.yams.service.GameEventService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games/{gameId}/events")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class GameEventController {

    private final GameEventService gameEventService;

    public GameEventController(GameEventService gameEventService) {
        this.gameEventService = gameEventService;
    }

    @GetMapping
    public List<GameEventHistoryEntry> history(@PathVariable Long gameId, @RequestParam(defaultValue = "20") int limit) {
        return gameEventService.listHistory(gameId, limit);
    }
}