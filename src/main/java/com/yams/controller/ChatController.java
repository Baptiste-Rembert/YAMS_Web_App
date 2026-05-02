package com.yams.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ChatController {

	@GetMapping("/ping")
	public String ping() {
		return "chat ok";
	}

	@PostMapping("/send")
	public String send(@RequestBody String payload) {
		// TODO: intégrer ChatService pour diffusion en temps réel
		return "received";
	}
}
