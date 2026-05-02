package com.yams.service;

import com.yams.model.User;
import com.yams.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User login(String username) {
        return findOrCreate(username);
    }

    @Transactional
    public User register(String username) {
        return findOrCreate(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    private User findOrCreate(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Le pseudo ne peut pas être vide.");
        }

        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    return userRepository.save(newUser);
                });
    }
}