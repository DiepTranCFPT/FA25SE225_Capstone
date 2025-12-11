package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.ExamAskingRequest;
import com.fa25se225.capstone.dto.response.StudentExamDashboardResponse;
import com.fa25se225.capstone.entity.StudentProfile;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.StudentProfileRepository;
import com.fa25se225.capstone.service.StudentDashboardService;
import com.fa25se225.capstone.service.v2.impl.SseNotificationService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private StudentDashboardService studentDashboardService;


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
