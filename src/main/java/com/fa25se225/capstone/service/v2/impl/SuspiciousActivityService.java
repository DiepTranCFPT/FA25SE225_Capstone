package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import com.fa25se225.capstone.entity.v2.SuspiciousActivityLog;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.repository.v2.SuspiciousActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuspiciousActivityService {

    private final ExamAttemptV2Repository attemptRepository;
    private final SuspiciousActivityLogRepository logRepository;

    /**
     * REQUIRES_NEW: Treo transaction hiện tại (nếu có), tạo một transaction MỚI.
     * Transaction mới này sẽ commit độc lập, ngay cả khi transaction gọi nó bị rollback sau đó.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuspiciousActivity(String attemptId, String message) {
        try {
            // Chỉ cần lấy reference (proxy) để set khóa ngoại, không cần load full entity
            ExamAttemptV2 attemptReference = attemptRepository.getReferenceById(attemptId);

            SuspiciousActivityLog logEntry = SuspiciousActivityLog.builder()
                    .examAttempt(attemptReference)
                    .message(message)
                    .build();

            logRepository.save(logEntry);
            log.warn("Suspicious activity logged for attempt {}: {}", attemptId, message);

        } catch (Exception e) {
            log.error("Failed to log suspicious activity for attempt {}", attemptId, e);
        }
    }
}
