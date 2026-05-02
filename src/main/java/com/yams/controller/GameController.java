package com.yams.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpSession;

import com.yams.model.Game;
import com.yams.model.Player;
import com.yams.model.User;
import com.yams.service.GameService;
import com.yams.service.SessionAuthService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class GameController {

	private final GameService gameService;
	private final SessionAuthService sessionAuthService;

	public GameController(GameService gameService, SessionAuthService sessionAuthService) {
		this.gameService = gameService;
		this.sessionAuthService = sessionAuthService;
	}

	@GetMapping("/ping")
	public String ping() {
		return "games ok";
	}

	@PostMapping
	public Game createGame() {
		return gameService.createGame();
	}

	@GetMapping
	public List<Game> listGames() {
		return gameService.listGames();
	}

	@PostMapping("/{id}/join")
	public Player joinGame(@PathVariable Long id, @RequestBody JoinRequest req, HttpSession session) {
		String username = req.username();
		if (username == null || username.isBlank()) {
			User currentUser = sessionAuthService.currentUser(session)
					.orElseThrow(() -> new IllegalArgumentException("Username requis ou session de connexion absente"));
			username = currentUser.getUsername();
		}
		return gameService.joinGame(id, username);
	}

	@GetMapping("/{id}")
	public GameDetail getGame(@PathVariable Long id) {
		Game game = gameService.getGame(id).orElseThrow(() -> new IllegalArgumentException("Game not found"));
		List<Player> players = gameService.getPlayersForGame(id);
		List<PlayerInfo> infos = players.stream().map(p -> new PlayerInfo(p.getId(), p.getUserId())).collect(Collectors.toList());
		return new GameDetail(game.getId(), game.isStarted(), game.getCurrentPlayerId(), infos);
	}

	@PostMapping("/{id}/start")
	public GameDetail startGame(@PathVariable Long id) {
		Game game = gameService.startGame(id);
		List<Player> players = gameService.getPlayersForGame(id);
		List<PlayerInfo> infos = players.stream().map(p -> new PlayerInfo(p.getId(), p.getUserId())).collect(Collectors.toList());
		return new GameDetail(game.getId(), game.isStarted(), game.getCurrentPlayerId(), infos);
	}

	@PostMapping("/{id}/restart")
	public GameDetail restartGame(@PathVariable Long id) {
		Game game = gameService.restartGame(id);
		List<Player> players = gameService.getPlayersForGame(id);
		List<PlayerInfo> infos = players.stream().map(p -> new PlayerInfo(p.getId(), p.getUserId())).collect(Collectors.toList());
		return new GameDetail(game.getId(), game.isStarted(), game.getCurrentPlayerId(), infos);
	}

	public static record JoinRequest(String username) {}
	public static record PlayerInfo(Long playerId, Long userId) {}
	public static record GameDetail(Long id, boolean started, Long currentPlayerId, List<PlayerInfo> players) {}
}
