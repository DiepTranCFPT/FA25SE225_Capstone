package com.fa25se225.capstone.configuration;

import java.security.Principal;
import java.util.List;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

public class UserHeaderInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> ids = accessor.getNativeHeader("X-USER-ID");
            String userId = (ids != null && !ids.isEmpty()) ? ids.get(0) : "anonymous";
            accessor.setUser(new SimplePrincipal(userId));
        }
        return message;
    }

    static class SimplePrincipal implements Principal {
        private final String name;
        SimplePrincipal(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
}
