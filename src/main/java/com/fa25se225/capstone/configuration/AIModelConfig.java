package com.fa25se225.capstone.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIModelConfig {

    @Bean(name = "chatClientWithoutChatMemory")
    public ChatClient chatClientWithoutChatMemory(ChatClient.Builder chatClientBuilder){
        return chatClientBuilder
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.3)
                        .build())
                .build();
    }
}
