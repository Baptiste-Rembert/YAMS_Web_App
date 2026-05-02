package com.yams.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ConnectedUserService {

    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    public void userLoggedIn(String username) {
        if (username != null && !username.isBlank()) {
            activeUsers.add(username);
        }
    }

    public void userLoggedOut(String username) {
        if (username != null && !username.isBlank()) {
            activeUsers.remove(username);
        }
    }

    public List<String> listActiveUsernames() {
        return activeUsers.stream().sorted().collect(Collectors.toCollection(ArrayList::new));
    }
}
