package com.yams.controller;

import com.yams.model.Game;
import com.yams.repository.GameRepository;
import com.yams.service.ConnectedUserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lobby")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class AdminController {

    private final ConnectedUserService connectedUserService;
    private final GameRepository gameRepository;

    public AdminController(ConnectedUserService connectedUserService, GameRepository gameRepository) {
        this.connectedUserService = connectedUserService;
        this.gameRepository = gameRepository;
    }

    @GetMapping("/connected")
    public List<String> connectedUsers() {
        return connectedUserService.listActiveUsernames();
    }

    @GetMapping("/games")
    public List<Game> listGames() {
        return gameRepository.findAll();
    }
}
