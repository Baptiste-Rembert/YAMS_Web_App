package com.yams.controller;

import com.yams.model.User;
import com.yams.service.AuthService;
import com.yams.service.SessionAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final SessionAuthService sessionAuthService;

    // Injection par constructeur
    public AuthController(AuthService authService, SessionAuthService sessionAuthService) {
        this.authService = authService;
        this.sessionAuthService = sessionAuthService;
    }

    @PostMapping("/auth/login")
    public User login(@RequestBody User user, HttpSession session) {
        return sessionAuthService.login(user.getUsername(), session);
    }

    @PostMapping("/auth/register")
    public User register(@RequestBody User user, HttpSession session) {
        return sessionAuthService.register(user.getUsername(), session);
    }

    @GetMapping("/auth/me")
    public ResponseEntity<User> me(HttpSession session) {
        return sessionAuthService.currentUser(session)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        sessionAuthService.logout(session);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hello")
    public String hello() {
        return "Le serveur Spring Boot est bien réveillé !";
    }
}