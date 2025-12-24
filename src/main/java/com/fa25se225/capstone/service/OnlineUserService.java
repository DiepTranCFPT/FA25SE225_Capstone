package com.fa25se225.capstone.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    // email -> số session đang mở
    private final Map<String, Integer> onlineUsers = new ConcurrentHashMap<>();

    public void userConnected(String email) {
        onlineUsers.merge(email, 1, Integer::sum);
    }

    public void userDisconnected(String email) {
        onlineUsers.computeIfPresent(email, (k, v) -> v > 1 ? v - 1 : null);
    }

    public boolean isOnline(String email) {
        return onlineUsers.containsKey(email);
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers.keySet();
    }
}

