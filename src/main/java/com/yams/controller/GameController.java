package com.yams.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.yams.model.Game;
import com.yams.model.Player;
import com.yams.service.GameService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class GameController {

	private final GameService gameService;

	public GameController(GameService gameService) {
		this.gameService = gameService;
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
	public Player joinGame(@PathVariable Long id, @RequestBody JoinRequest req) {
		return gameService.joinGame(id, req.username());
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

	public static record JoinRequest(String username) {}
	public static record PlayerInfo(Long playerId, Long userId) {}
	public static record GameDetail(Long id, boolean started, Long currentPlayerId, List<PlayerInfo> players) {}
}
