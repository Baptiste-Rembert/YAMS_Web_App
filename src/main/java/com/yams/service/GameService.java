package com.yams.service;

import com.yams.model.Game;
import com.yams.model.Player;
import com.yams.model.User;
import com.yams.repository.GameRepository;
import com.yams.repository.PlayerRepository;
import com.yams.service.ScorecardService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final AuthService authService;
    private final ScorecardService scorecardService;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, AuthService authService, ScorecardService scorecardService) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.authService = authService;
        this.scorecardService = scorecardService;
    }

    public Game createGame() {
        Game g = new Game();
        return gameRepository.save(g);
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
        return playerRepository.save(p);
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

        return saved;
    }
}
