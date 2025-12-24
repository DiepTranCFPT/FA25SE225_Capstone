package com.fa25se225.capstone.configuration;

import com.fa25se225.capstone.service.OnlineUserService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketPresenceListener {

    private final OnlineUserService onlineUserService;

    public WebSocketPresenceListener(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            onlineUserService.userConnected(principal.getName());
            System.out.println("🟢 ONLINE: " + principal.getName());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            onlineUserService.userDisconnected(principal.getName());
            System.out.println("🔴 OFFLINE: " + principal.getName());
        }
    }
}
