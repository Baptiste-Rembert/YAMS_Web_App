package com.yams.service;

import com.yams.model.Game;
import com.yams.model.GameEvent;
import com.yams.model.Player;
import com.yams.model.Turn;
import com.yams.repository.GameRepository;
import com.yams.repository.PlayerRepository;
import com.yams.repository.TurnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TurnService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final TurnRepository turnRepository;
    private final GameEventService eventService;

    public TurnService(GameRepository gameRepository, PlayerRepository playerRepository, TurnRepository turnRepository, GameEventService eventService) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.turnRepository = turnRepository;
        this.eventService = eventService;
    }

    @Transactional
    public Turn rollDice(Long gameId) {
        Game g = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        if (!g.isStarted()) throw new IllegalStateException("Game not started");
        Long playerId = g.getCurrentPlayerId();
        if (playerId == null) throw new IllegalStateException("No current player set");

        Optional<Turn> opt = turnRepository.findFirstByGameIdAndPlayerIdAndCompletedFalse(gameId, playerId);
        Turn t;
        if (opt.isPresent()) {
            t = opt.get();
            if (t.getRolls() >= 3) throw new IllegalStateException("No rolls remaining");
            t.setRolls(t.getRolls() + 1);
        } else {
            t = new Turn();
            t.setGameId(gameId);
            t.setPlayerId(playerId);
            t.setRolls(1);
            t.setCompleted(false);
        }

        // roll five dice
        Random rnd = new Random();
        int[] diceArr = new int[5];
        for (int i = 0; i < 5; i++) diceArr[i] = rnd.nextInt(6) + 1;
        String diceCsv = Arrays.stream(diceArr).mapToObj(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        t.setDice(diceCsv);
        Turn saved = turnRepository.save(t);

        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("dice", Arrays.stream(diceArr).boxed().toList());
        data.put("rolls", saved.getRolls());
        GameEvent evt = new GameEvent("ROLLED", gameId, data);
        eventService.sendEvent(gameId, evt);

        return saved;
    }

    @Transactional
    public Turn rerollDice(Long gameId, int[] indices) {
        Game g = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        if (!g.isStarted()) throw new IllegalStateException("Game not started");
        Long playerId = g.getCurrentPlayerId();
        if (playerId == null) throw new IllegalStateException("No current player set");

        Turn t = turnRepository.findFirstByGameIdAndPlayerIdAndCompletedFalse(gameId, playerId)
                .orElseThrow(() -> new IllegalStateException("No active turn to reroll"));
        if (t.getRolls() >= 3) throw new IllegalStateException("No rolls remaining");

        String[] parts = (t.getDice() == null) ? new String[0] : t.getDice().split(",");
        int[] diceArr = new int[5];
        for (int i = 0; i < 5; i++) {
            diceArr[i] = (i < parts.length && parts[i].length() > 0) ? Integer.parseInt(parts[i]) : 0;
        }

        Random rnd = new Random();
        for (int idx : indices) {
            if (idx < 0 || idx >= diceArr.length) throw new IllegalArgumentException("Index out of range: " + idx);
            diceArr[idx] = rnd.nextInt(6) + 1;
        }

        t.setRolls(t.getRolls() + 1);
        String diceCsv = Arrays.stream(diceArr).mapToObj(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        t.setDice(diceCsv);
        Turn saved = turnRepository.save(t);

        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("dice", Arrays.stream(diceArr).boxed().toList());
        data.put("rolls", saved.getRolls());
        GameEvent evt = new GameEvent("REROLLED", gameId, data);
        eventService.sendEvent(gameId, evt);

        return saved;
    }

    @Transactional
    public Game endTurn(Long gameId) {
        Game g = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        if (!g.isStarted()) throw new IllegalStateException("Game not started");
        Long playerId = g.getCurrentPlayerId();
        List<Player> players = playerRepository.findByGameId(gameId);
        if (players.isEmpty()) throw new IllegalStateException("No players");

        Optional<Turn> opt = turnRepository.findFirstByGameIdAndPlayerIdAndCompletedFalse(gameId, playerId);
        opt.ifPresent(turn -> {
            turn.setCompleted(true);
            turnRepository.save(turn);
        });

        int idx = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(playerId)) {
                idx = i;
                break;
            }
        }
        int nextIdx = (idx + 1) % players.size();
        Long prevPlayer = playerId;
        g.setCurrentPlayerId(players.get(nextIdx).getId());
        Game savedGame = gameRepository.save(g);

        Map<String, Object> data = new HashMap<>();
        data.put("prevPlayerId", prevPlayer);
        data.put("currentPlayerId", savedGame.getCurrentPlayerId());
        GameEvent evt = new GameEvent("TURN_ENDED", gameId, data);
        eventService.sendEvent(gameId, evt);

        return savedGame;
    }
}
