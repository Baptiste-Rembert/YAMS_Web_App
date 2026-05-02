package com.yams.service;

import com.yams.model.User;
import com.yams.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Map;

import java.time.Instant;
import java.util.Optional;

@Service
public class SessionAuthService {

    public static final String SESSION_USER_ID = "yams.currentUserId";
    public static final String SESSION_USER_NAME = "yams.currentUsername";
    public static final String SESSION_AUTHENTICATED_AT = "yams.authenticatedAt";

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ConnectedUserService connectedUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public SessionAuthService(AuthService authService, UserRepository userRepository, ConnectedUserService connectedUserService, SimpMessagingTemplate messagingTemplate) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.connectedUserService = connectedUserService;
        this.messagingTemplate = messagingTemplate;
    }

    public User login(String username, HttpSession session) {
        User user = authService.login(username);
        store(session, user);
        connectedUserService.userLoggedIn(user.getUsername());
        try {
            messagingTemplate.convertAndSend("/topic/connected", Map.of("users", connectedUserService.listActiveUsernames()));
        } catch (Exception e) {
            // best-effort notify
        }
        return user;
    }

    public User register(String username, HttpSession session) {
        User user = authService.register(username);
        store(session, user);
        connectedUserService.userLoggedIn(user.getUsername());
        try {
            messagingTemplate.convertAndSend("/topic/connected", Map.of("users", connectedUserService.listActiveUsernames()));
        } catch (Exception e) {
            // best-effort notify
        }
        return user;
    }

    public Optional<User> currentUser(HttpSession session) {
        if (session == null) {
            return Optional.empty();
        }
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId instanceof Long id) {
            return userRepository.findById(id);
        }
        if (userId instanceof Number number) {
            return userRepository.findById(number.longValue());
        }
        return Optional.empty();
    }

    public void logout(HttpSession session) {
        if (session != null) {
            Object usernameAttr = session.getAttribute(SESSION_USER_NAME);
            if (usernameAttr instanceof String username) {
                connectedUserService.userLoggedOut(username);
                try {
                    messagingTemplate.convertAndSend("/topic/connected", Map.of("users", connectedUserService.listActiveUsernames()));
                } catch (Exception e) {
                    // best-effort notify
                }
            }
            session.invalidate();
        }
    }

    private void store(HttpSession session, User user) {
        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USER_NAME, user.getUsername());
        session.setAttribute(SESSION_AUTHENTICATED_AT, Instant.now().toString());
    }
}