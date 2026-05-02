package com.yams.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yams.model.Scorecard;
import com.yams.model.Player;
import com.yams.model.GameEvent;
import com.yams.model.Turn;
import com.yams.repository.ScorecardRepository;
import com.yams.repository.PlayerRepository;
import com.yams.repository.TurnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ScorecardService {

    private final ScorecardRepository scorecardRepository;
    private final PlayerRepository playerRepository;
    private final TurnRepository turnRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameEventService eventService;

    public ScorecardService(ScorecardRepository scorecardRepository, PlayerRepository playerRepository, TurnRepository turnRepository, GameEventService eventService) {
        this.scorecardRepository = scorecardRepository;
        this.playerRepository = playerRepository;
        this.turnRepository = turnRepository;
        this.eventService = eventService;
    }

    @Transactional
    public void createForGame(Long gameId) {
        List<Player> players = playerRepository.findByGameId(gameId);
        for (Player p : players) {
            scorecardRepository.findByGameIdAndPlayerId(gameId, p.getId()).orElseGet(() -> {
                Scorecard sc = new Scorecard();
                sc.setGameId(gameId);
                sc.setPlayerId(p.getId());
                sc.setScores("{}");
                return scorecardRepository.save(sc);
            });
        }
    }

    @Transactional
    public Scorecard updateScore(Long gameId, Long playerId, String category, Integer score) {
        Scorecard sc = scorecardRepository.findByGameIdAndPlayerId(gameId, playerId)
                .orElseThrow(() -> new IllegalArgumentException("Scorecard not found"));

        // Validate against last turn if available
        Optional<Turn> lastTurn = turnRepository.findTopByGameIdAndPlayerIdOrderByIdDesc(gameId, playerId);
        if (lastTurn.isPresent()) {
            int[] dice = parseDiceCsv(lastTurn.get().getDice());
            int expected = computeCategoryScore(dice, category);
            if (!(score == 0 || score == expected)) {
                throw new IllegalArgumentException("Invalid score for category: expected " + expected + " or 0");
            }
        }

        Map<String, Integer> map;
        try {
            if (sc.getScores() == null || sc.getScores().isBlank()) map = new HashMap<>();
            else map = objectMapper.readValue(sc.getScores(), new TypeReference<Map<String, Integer>>(){});
        } catch (Exception e) {
            map = new HashMap<>();
        }

        map.put(category.toUpperCase(Locale.ROOT), score);
        try {
            sc.setScores(objectMapper.writeValueAsString(map));
        } catch (Exception e) {
            sc.setScores("{}");
        }

        Scorecard saved = scorecardRepository.save(sc);

        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("category", category);
        data.put("score", score);
        GameEvent evt = new GameEvent("SCORE_UPDATED", gameId, data);
        eventService.sendEvent(gameId, evt);

        return saved;
    }

    public Scorecard get(Long gameId, Long playerId) {
        return scorecardRepository.findByGameIdAndPlayerId(gameId, playerId).orElseThrow(() -> new IllegalArgumentException("Scorecard not found"));
    }

    public ScorecardSummary computeSummary(Long gameId, Long playerId) {
        Scorecard sc = get(gameId, playerId);
        Map<String, Integer> map;
        try {
            if (sc.getScores() == null || sc.getScores().isBlank()) map = new HashMap<>();
            else {
                Map<String, Integer> tmp = objectMapper.readValue(sc.getScores(), new TypeReference<Map<String, Integer>>(){});
                map = new HashMap<>();
                for (Map.Entry<String,Integer> e : tmp.entrySet()) map.put(e.getKey().toUpperCase(Locale.ROOT), e.getValue());
            }
        } catch (Exception e) {
            map = new HashMap<>();
        }

        int upperTotal = 0;
        upperTotal += map.getOrDefault("ONES", 0);
        upperTotal += map.getOrDefault("TWOS", 0);
        upperTotal += map.getOrDefault("THREES", 0);
        upperTotal += map.getOrDefault("FOURS", 0);
        upperTotal += map.getOrDefault("FIVES", 0);
        upperTotal += map.getOrDefault("SIXES", 0);
        int upperBonus = (upperTotal >= 63) ? 35 : 0;

        int lowerTotal = 0;
        String[] lowerCats = {"THREE_OF_A_KIND","FOUR_OF_A_KIND","FULL_HOUSE","SMALL_STRAIGHT","LARGE_STRAIGHT","CHANCE","YAHTZEE"};
        for (String c : lowerCats) lowerTotal += map.getOrDefault(c, 0);

        int total = upperTotal + upperBonus + lowerTotal;

        return new ScorecardSummary(map, upperTotal, upperBonus, lowerTotal, total);
    }

    private int[] parseDiceCsv(String csv) {
        if (csv == null || csv.isBlank()) return new int[0];
        String[] parts = csv.split(",");
        int[] dice = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try { dice[i] = Integer.parseInt(parts[i]); } catch (Exception e) { dice[i] = 0; }
        }
        return dice;
    }

    private int computeCategoryScore(int[] dice, String categoryRaw) {
        String category = (categoryRaw == null) ? "" : categoryRaw.toUpperCase(Locale.ROOT);
        int[] counts = new int[7];
        int sum = 0;
        for (int d : dice) { if (d >= 1 && d <= 6) { counts[d]++; sum += d; } }

        switch (category) {
            case "ONES": return counts[1] * 1;
            case "TWOS": return counts[2] * 2;
            case "THREES": return counts[3] * 3;
            case "FOURS": return counts[4] * 4;
            case "FIVES": return counts[5] * 5;
            case "SIXES": return counts[6] * 6;
            case "THREE_OF_A_KIND":
                for (int i = 1; i <= 6; i++) if (counts[i] >= 3) return sum;
                return 0;
            case "FOUR_OF_A_KIND":
                for (int i = 1; i <= 6; i++) if (counts[i] >= 4) return sum;
                return 0;
            case "FULL_HOUSE":
                boolean has3 = false, has2 = false;
                for (int i = 1; i <= 6; i++) { if (counts[i] == 3) has3 = true; if (counts[i] == 2) has2 = true; }
                return (has3 && has2) ? 25 : 0;
            case "SMALL_STRAIGHT":
                if ((counts[1] >=1 && counts[2] >=1 && counts[3] >=1 && counts[4] >=1) ||
                    (counts[2] >=1 && counts[3] >=1 && counts[4] >=1 && counts[5] >=1) ||
                    (counts[3] >=1 && counts[4] >=1 && counts[5] >=1 && counts[6] >=1)) return 30;
                return 0;
            case "LARGE_STRAIGHT":
                if ((counts[1]==1 && counts[2]==1 && counts[3]==1 && counts[4]==1 && counts[5]==1) ||
                    (counts[2]==1 && counts[3]==1 && counts[4]==1 && counts[5]==1 && counts[6]==1)) return 40;
                return 0;
            case "YAHTZEE":
                for (int i = 1; i <= 6; i++) if (counts[i] == 5) return 50;
                return 0;
            case "CHANCE":
                return sum;
            default:
                return 0;
        }
    }
}
