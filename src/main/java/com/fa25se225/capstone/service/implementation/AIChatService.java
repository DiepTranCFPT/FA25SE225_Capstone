package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.ExamAskingRequest;
import com.fa25se225.capstone.entity.StudentProfile;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.StudentProfileRepository;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.service.StudentDashboardService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AIChatService {

    @Autowired
    @Qualifier("chatClientWithChatInMemory")
    private ChatClient primaryChatClient;

    @Autowired
    @Qualifier("chatClientWithChatInMemoryUsingLiteModel")
    private ChatClient secondaryChatClient;

    @Autowired
    private InMemoryChatMemoryRepository inMemoryChatMemoryRepository;

    @Autowired
    private  AccountUtil accountUtil;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ExamAttemptV2Repository examAttemptV2Repository;

    @Autowired
    private StudentDashboardService studentDashboardService;


    private static class UserRequestInfo {
        final LocalDate date;
        final AtomicInteger count;

        UserRequestInfo(LocalDate date) {
            this.date = date;
            this.count = new AtomicInteger(0);
        }
    }

    private final ConcurrentHashMap<String, UserRequestInfo> dailyRequestMap = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 0 0 * * ?")
    protected void clearDailyRequests() {
        dailyRequestMap.clear();
    }


    private Set<String> conversationIds = new HashSet<>();

    public Flux<String> examAsk(ExamAskingRequest request){
        String conversationId = request.getDoneBy().concat(request.getAttemptId());
        conversationIds.add(conversationId);
        String userAsking = String.format(
                """
                        Question : %s
                        Student answer : %s
                        Student asking : %s
                        """,
                request.getQuestionContent(), request.getStudentAnswer(), request.getStudentAsking()
        );
        String systemText = "You are an expert at answering AP exam questions. (Only answer questions that are related to the questions and answers students produce.)";

        return callChatClient(primaryChatClient, systemText, userAsking, conversationId)
                .onErrorResume(e -> {
                    log.warn("Primary model failed for conversation {}. Switching to Lite model. Error: {}", conversationId, e.getMessage());
                    return callChatClient(secondaryChatClient, systemText, userAsking, conversationId);
                });

    }


    @Scheduled(cron = "0 0 4 * * ?")
    protected void clearChatMemory(){
        conversationIds.stream().forEach(s -> inMemoryChatMemoryRepository.deleteByConversationId(s));
    }


    public Flux<String> chatStudentDashBoard(String prompt) {
        User student = accountUtil.getCurrentUser();
        StudentProfile studentProfile = studentProfileRepository.findByUserId(student.getId()).get();
        String conversationId = studentProfile.getId();
        conversationIds.add(conversationId);
        String studentInfo = studentDashboardService.getStudentExamDashboard().toString();
        String userAsking = String.format(
                """
                        Student Goal : %s
                        Student information : %s
                        Student asking : %s
                        """,
                studentProfile.getGoal(), studentInfo, prompt
        );
        String systemText = "You are an expert at answering AP exam questions. (Only answer questions that are related to the questions and answers students produce.)";

        return callChatClient(primaryChatClient, systemText, userAsking, conversationId)
                .onErrorResume(e -> {
                    log.warn("Primary model failed for conversation {}. Switching to Lite model. Error: {}", conversationId, e.getMessage());
                    return callChatClient(secondaryChatClient, systemText, userAsking, conversationId);
                });

    }

    @Scheduled(cron = "0 0 23 * * *")
    protected void createRecommendForStudents() {
        List<User> students = examAttemptV2Repository.findDistinctUsersAttemptedToday();
        for (User student : students) {
            setRecommendOfStudentByAI(student.getId());
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sleep interrupted while processing student {}: {}", student.getId(), e.getMessage());
            }

        }

    }

    @CacheEvict(value = "student_exam_dashboard", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public void createRecommendForCurrentStudent() {
        String userId = accountUtil.getCurrentUser().getId();
        AtomicBoolean allowed = new AtomicBoolean(false);
        LocalDate today = LocalDate.now();

        dailyRequestMap.compute(userId, (k, info) -> {
            if (info == null || !info.date.equals(today)) {
                UserRequestInfo newInfo = new UserRequestInfo(today);
                newInfo.count.incrementAndGet();
                allowed.set(true);
                return newInfo;
            }
            if (info.count.get() < 3) {
                info.count.incrementAndGet();
                allowed.set(true);
            }
            return info;
        });

        if (!allowed.get()) {
            log.warn("Daily recommend limit reached for user {} (max 3 per day).", userId);
            throw new AppException(ErrorCode.EXCEED_REQUEST);
        }

        setRecommendOfStudentByAI(userId);
    }


    private void setRecommendOfStudentByAI(String studentId) {
        StudentProfile studentProfile = studentProfileRepository.findByUserId(studentId).get();
        String studentInfo = studentDashboardService.getStudentExamDashboard().toString();
        String userText = String.format(
                """
                        Student Goal : %s
                        Student information : %s
                        """,
                studentProfile.getGoal(), studentInfo
        );
        String systemText = """
                You are an AP exam expert. Analyze and guide students based on their information. (~100 words)
                If the student doesn't have a goal yet, still reply, but remind them to update their goal profile.
                """;

        String recommend =  primaryChatClient.prompt().system(systemText)
                                            .user(userText)
                                            .call()
                                            .content();
        studentProfile.setRecommend(recommend);
        studentProfileRepository.save(studentProfile);
    }

    private Flux<String> callChatClient(ChatClient client, String systemText, String userText, String conversationId) {
        return client.prompt()
                .system(systemText)
                .user(userText)
                .advisors(advisorSpec -> advisorSpec.param("CONVERSATION_ID", conversationId))
                .stream()
                .content()
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(2))
                                .filter(this::isRetryable)
                                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying chat for conversation {} due to exception {}", conversationId, retrySignal.failure()))
                .onRetryExhaustedThrow((spec, sig) -> sig.failure())
                 );
    }

    private boolean isRetryable(Throwable ex) {
        return ex instanceof WebClientResponseException wex
                && (wex.getStatusCode().value() == 429 || wex.getStatusCode().is5xxServerError());
    }


}
