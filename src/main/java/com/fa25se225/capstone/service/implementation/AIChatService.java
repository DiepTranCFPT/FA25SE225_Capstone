package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.ExamAskingRequest;
import com.fa25se225.capstone.utils.AccountUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AIChatService {
    @Autowired
    @Qualifier("chatClientWithChatInMemory")
    private ChatClient chatClient;

    @Autowired
    private InMemoryChatMemoryRepository inMemoryChatMemoryRepository;

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
        return chatClient.prompt()
                .system("You are an expert at answering AP exam questions. (Only answer questions that are related to the questions and answers students produce.)")
                .user(userAsking)
                .advisors(advisorSpec -> advisorSpec.param("CONVERSATION_ID", conversationId))
                .stream()
                .content();

    }

    @Scheduled(cron = "0 0 4 * * ?")
    protected void clearChatMemory(){
        conversationIds.stream().forEach(s -> inMemoryChatMemoryRepository.deleteByConversationId(s));
    }



}
