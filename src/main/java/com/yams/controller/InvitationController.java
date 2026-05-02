package com.yams.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class InvitationController {

	@GetMapping("/ping")
	public String ping() {
		return "invitations ok";
	}

	@PostMapping
	public String createInvitation(@RequestBody String payload) {
		// TODO: appeler InvitationService pour créer une invitation
		return "create-invitation: not implemented";
	}
}
