package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final AccountUtil accountUtil;

    public SseEmitter subscribe() {
        SseEmitter sseEmitter = new SseEmitter(60 * 60000L);
        String currentUserId = accountUtil.getCurrentUser().getId();

        emitters.put(currentUserId, sseEmitter);

        try {
            sseEmitter.send(SseEmitter.event().name("INIT").data("Connected"));
        } catch (IOException e) {
            log.error("Error sending INIT event to user {}", currentUserId, e);
            emitters.remove(currentUserId);
            return sseEmitter;
        }

        sseEmitter.onCompletion(
                () -> {
                    log.info("Sse {} complete", currentUserId);
                    emitters.remove(currentUserId, sseEmitter);
                }
        );
        sseEmitter.onTimeout(
                () -> {
                    log.info("Sse {} timeout", currentUserId);
                    sseEmitter.complete();
                    emitters.remove(currentUserId, sseEmitter);
                }
        );
        sseEmitter.onError((e) -> {
            log.error("Sse {} error", currentUserId, e);
            sseEmitter.completeWithError(e);
            emitters.remove(currentUserId, sseEmitter);
        });
        return sseEmitter;
    }

    public void sendSSe(String userId, Object data, String name){
        SseEmitter sseEmitter = emitters.get(userId);
        if(Objects.nonNull(sseEmitter)){
            try {
                log.info("Sending {} to {}", name, userId);
                sseEmitter.send(
                        SseEmitter.event()
                                .name(name)
                                .data(data)
                );
            } catch (Exception e) {
                log.error("Error sending SSE to user {}", userId, e);
                emitters.remove(userId, sseEmitter);
            }
        } else {
            log.warn("SSE Warning: User {} not found in emitters map. Notification skipped.", userId);
        }

    }
}