package com.yams.controller;

import com.yams.model.User;
import com.yams.service.AuthService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AuthController {

    private final AuthService authService;

    // Injection par constructeur
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public User login(@RequestBody User user) {
        return authService.login(user.getUsername());
    }

    @PostMapping("/auth/register")
    public User register(@RequestBody User user) {
        // For now registration behaves like login: create if not exists
        return authService.login(user.getUsername());
    }

    @GetMapping("/hello")
    public String hello() {
        return "Le serveur Spring Boot est bien réveillé !";
    }
}