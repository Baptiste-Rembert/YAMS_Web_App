package com.yams.controller;

import com.yams.model.Scorecard;
import com.yams.service.ScorecardService;
import com.yams.service.ScorecardSummary;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/games/{gameId}/scorecard")
public class ScorecardController {

    private final ScorecardService scorecardService;

    public ScorecardController(ScorecardService scorecardService) {
        this.scorecardService = scorecardService;
    }

    @GetMapping("/{playerId}")
    public Scorecard get(@PathVariable Long gameId, @PathVariable Long playerId) {
        return scorecardService.get(gameId, playerId);
    }

    @PostMapping("/{playerId}/score")
    public Scorecard updateScore(@PathVariable Long gameId, @PathVariable Long playerId, @RequestBody ScoreUpdateRequest req) {
        return scorecardService.updateScore(gameId, playerId, req.category(), req.score());
    }

    @GetMapping("/{playerId}/summary")
    public ScorecardSummary summary(@PathVariable Long gameId, @PathVariable Long playerId) {
        return scorecardService.computeSummary(gameId, playerId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    public static record ScoreUpdateRequest(String category, Integer score) {}
}
