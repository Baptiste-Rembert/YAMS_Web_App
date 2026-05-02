package com.yams.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class LeaderboardController {

	@GetMapping
	public List<String> top() {
		// TODO: intégrer LeaderboardService pour retourner le classement réel
		return Collections.emptyList();
	}
}
