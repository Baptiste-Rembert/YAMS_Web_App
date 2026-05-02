package com.yams.controller;

import com.yams.model.Invitation;
import com.yams.service.InvitationService;
import com.yams.service.SessionAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class InvitationController {

	private final InvitationService invitationService;
	private final SessionAuthService sessionAuthService;

	public InvitationController(InvitationService invitationService, SessionAuthService sessionAuthService) {
		this.invitationService = invitationService;
		this.sessionAuthService = sessionAuthService;
	}

	@GetMapping("/ping")
	public String ping() {
		return "invitations ok";
	}

	@PostMapping
	public Invitation createInvitation(@RequestBody Map<String, Object> payload, HttpSession session) {
		String toUsername = payload.getOrDefault("toUsername", "").toString();
		Object gameIdObj = payload.get("gameId");
		Long gameId = null;
		if (gameIdObj instanceof Number n) gameId = ((Number) gameIdObj).longValue();
		if (gameIdObj instanceof String s) {
			try { gameId = Long.parseLong(s); } catch (Exception ignored) {}
		}

		String fromUsername = sessionAuthService.currentUser(session).map(u -> u.getUsername()).orElse("anonymous");
		return invitationService.createInvitation(fromUsername, toUsername, gameId);
	}
}
