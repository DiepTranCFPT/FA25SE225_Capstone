package com.fa25se225.capstone.service.v2.impl;


import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.ExamAttemptV2Mapper;
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

    private final ExamAttemptV2Repository attemptRepository;
    private final ExamAttemptV2Mapper attemptMapper;

    public SseEmitter subscribe(String attemptId) {
        SseEmitter emitter = new SseEmitter(15 * 60 * 1000L);

        ExamAttemptV2 attempt = attemptRepository.findById(attemptId).orElse(null);

        if (Objects.nonNull(attempt) && attempt.getStatus() == AttemptStatusV2.COMPLETED) {
            log.info("Attempt {} is ALREADY completed. Sending immediate notification.", attemptId);
            try {
                ExamAttemptV2Response response = attemptMapper.toResponse(attempt);
                emitter.send(SseEmitter.event().name("grading_complete").data(response));
                emitter.complete();
                return emitter;
            } catch (IOException e) {
                log.error("Error sending immediate notification", e);
            }
        }

        this.emitters.put(attemptId, emitter);

        setupEmitterCallbacks(emitter, attemptId);

        try {
            emitter.send(SseEmitter.event().name("subscribed").data("Waiting for grading..."));
        } catch (IOException e) {
            log.warn("Could not send subscription confirmation");
        }

        log.info("New SSE subscription waiting for attemptId: {}", attemptId);
        return emitter;
    }

    public void sendGradingCompleteNotification(String attemptId, ExamAttemptV2Response response) {
        SseEmitter emitter = this.emitters.get(attemptId);

        if (emitter == null) {
            log.info("No active emitter for attemptId: {}. Client might connect later.", attemptId);
            return;
        }

        try {
            log.info("Sending 'grading_complete' event to attemptId: {}", attemptId);
            emitter.send(SseEmitter.event().name("grading_complete").data(response));
            emitter.complete();
        } catch (IOException e) {
            log.warn("Failed to send event: {}", e.getMessage());
            emitter.completeWithError(e);
        } finally {
            this.emitters.remove(attemptId);
        }
    }

    private void setupEmitterCallbacks(SseEmitter emitter, String attemptId) {
        emitter.onCompletion(() -> this.emitters.remove(attemptId));
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitters.remove(attemptId);
        });
        emitter.onError(e -> this.emitters.remove(attemptId));
    }
}