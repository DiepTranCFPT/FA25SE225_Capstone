package com.fa25se225.capstone.service.v2.impl;


import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseNotificationService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ExamAttemptV2Repository examAttemptV2Repository;
    private final AccountUtil accountUtil;

    public SseEmitter subscribe(String attemptId) {

        var attempt = examAttemptV2Repository.findById(attemptId).orElseThrow(
                () -> new AppException(ErrorCode.EXAM_ATTEMPT_NOT_FOUND)
        );

        if(!accountUtil.getCurrentUser().getEmail().equals(attempt.getUser().getEmail())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        SseEmitter emitter = new SseEmitter(15 * 60 * 1000L);
        this.emitters.put(attemptId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE emitter completed for attemptId: {}", attemptId);
            this.emitters.remove(attemptId);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE emitter timed out for attemptId: {}", attemptId);
            emitter.complete();
            this.emitters.remove(attemptId);
        });
        emitter.onError(e -> {
            log.error("SSE emitter error for attemptId: {}: {}", attemptId, e.getMessage());
            this.emitters.remove(attemptId);
        });

        try {
            emitter.send(SseEmitter.event().name("subscribed").data("OK"));
        } catch (IOException e) {
            log.warn("Could not send subscription confirmation to attemptId: {}", attemptId);
        }

        log.info("New SSE subscription for attemptId: {}", attemptId);
        return emitter;
    }

    public void sendGradingCompleteNotification(String attemptId, ExamAttemptV2Response response) {
        SseEmitter emitter = this.emitters.get(attemptId);

        if (Objects.isNull(emitter)) {
            log.warn("No active SSE emitter found for attemptId: {}. Notification missed.", attemptId);
            return;
        }

        try {
            log.info("Sending 'grading_complete' event to attemptId: {}", attemptId);
            emitter.send(SseEmitter.event().name("grading_complete").data(response));
        } catch (IOException e) {
            log.warn("Failed to send 'grading_complete' event to attemptId: {}: {}", attemptId, e.getMessage());
        } finally {
            emitter.complete();
            this.emitters.remove(attemptId);
        }
    }
}
