package com.fa25se225.capstone.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Configuration
public class AIModelConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> restClientBuilder
                .requestFactory(ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS
                        .withReadTimeout(Duration.ofSeconds(120))
                        .withConnectTimeout(Duration.ofSeconds(60))
                ));
    }


    @Bean(name = "chatClientWithoutChatMemory")
    public ChatClient chatClientWithoutChatMemory(ChatClient.Builder chatClientBuilder){
        return chatClientBuilder
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.3)
                        .build())
                .build();
    }

    @Bean(name = "chatClientWithChatInMemory")
    public ChatClient chatClientWithChatInMemory(ChatClient.Builder chatClientBuilder, InMemoryChatMemoryRepository inMemoryChatMemoryRepository){
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(15)
                .chatMemoryRepository(inMemoryChatMemoryRepository)
                .build();

        return chatClientBuilder
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.3)
                        .build())
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build())
                .build();
    }

    @Bean(name = "chatClientWithChatInMemoryUsingLiteModel")
    public ChatClient chatClientWithChatInMemoryUsingLiteModel(ChatClient.Builder chatClientBuilder, InMemoryChatMemoryRepository inMemoryChatMemoryRepository){
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(5)
                .chatMemoryRepository(inMemoryChatMemoryRepository)
                .build();

        return chatClientBuilder
                .defaultOptions(ChatOptions.builder()
                        .model("gemini-2.5-flash-lite")
                        .temperature(0.3)
                        .build())
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build())
                .build();
    }

    @Bean(name = "chatClientWithOutChatInMemoryUsingLiteModel")
    public ChatClient chatClientWithOutChatInMemoryUsingLiteModel(ChatClient.Builder chatClientBuilder, InMemoryChatMemoryRepository inMemoryChatMemoryRepository){


        return  chatClientBuilder
                .defaultOptions(ChatOptions.builder()
                        .model("gemini-2.5-flash-lite")
                        .temperature(0.3)
                        .build())
                .build();
    }

    @Bean
    public InMemoryChatMemoryRepository MyChatInMemoryRepository(){
        return new InMemoryChatMemoryRepository();
    }

}
