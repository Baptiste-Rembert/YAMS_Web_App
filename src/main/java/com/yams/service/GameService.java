package com.yams.service;

import com.yams.model.Game;
import com.yams.model.Player;
import com.yams.model.GameEvent;
import com.yams.model.User;
import com.yams.repository.GameRepository;
import com.yams.repository.PlayerRepository;
import com.yams.service.ScorecardService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import java.util.HashMap;
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final AuthService authService;
    private final ScorecardService scorecardService;
    private final GameEventService eventService;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, AuthService authService, ScorecardService scorecardService, GameEventService eventService) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.authService = authService;
        this.scorecardService = scorecardService;
        this.eventService = eventService;
    }

    public Game createGame() {
        Game g = new Game();
        Game saved = gameRepository.save(g);
        eventService.sendEvent(saved.getId(), "GAME_CREATED", gameStateData(saved));
        return saved;
    }

    public List<Game> listGames() {
        return gameRepository.findAll();
    }

    public Optional<Game> getGame(Long id) {
        return gameRepository.findById(id);
    }

    public Player joinGame(Long gameId, String username) {
        Game g = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        User user = authService.login(username);
        Player p = new Player();
        p.setGameId(g.getId());
        p.setUserId(user.getId());
        Player saved = playerRepository.save(p);

        HashMap<String, Object> data = new HashMap<>();
        data.put("playerId", saved.getId());
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("totalPlayers", playerRepository.findByGameId(gameId).size());
        data.putAll(gameStateData(g));
        eventService.sendEvent(gameId, "PLAYER_JOINED", data);

        return saved;
    }

    public List<Player> getPlayersForGame(Long gameId) {
        return playerRepository.findByGameId(gameId);
    }

    public Game startGame(Long gameId) {
        Game g = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        List<Player> players = playerRepository.findByGameId(gameId);
        if (players.isEmpty()) {
            throw new IllegalStateException("Cannot start game without players");
        }
        g.setStarted(true);
        // choose first player as current player
        g.setCurrentPlayerId(players.get(0).getId());
        Game saved = gameRepository.save(g);

        // create scorecards for players
        scorecardService.createForGame(saved.getId());

        HashMap<String, Object> data = new HashMap<>();
        data.put("currentPlayerId", saved.getCurrentPlayerId());
        data.put("playerIds", players.stream().map(Player::getId).toList());
        data.putAll(gameStateData(saved));
        eventService.sendEvent(saved.getId(), "GAME_STARTED", data);

        return saved;
    }

    public Game restartGame(Long gameId) {
        Game g = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        List<Player> players = playerRepository.findByGameId(gameId);
        if (players.isEmpty()) {
            throw new IllegalStateException("Cannot restart game without players");
        }

        g.setStarted(true);
        g.setCurrentPlayerId(players.get(0).getId());
        Game saved = gameRepository.save(g);

        // reset scorecards and remove turns
        scorecardService.resetForGame(saved.getId());

        HashMap<String, Object> data = new HashMap<>();
        data.put("currentPlayerId", saved.getCurrentPlayerId());
        data.put("playerIds", players.stream().map(Player::getId).toList());
        data.putAll(gameStateData(saved));
        eventService.sendEvent(saved.getId(), "GAME_RESTARTED", data);

        return saved;
    }

    private HashMap<String, Object> gameStateData(Game game) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("gameId", game.getId());
        data.put("started", game.isStarted());
        data.put("currentPlayerId", game.getCurrentPlayerId());
        return data;
    }
}
