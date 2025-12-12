package com.fa25se225.capstone.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        GenericJackson2JsonRedisSerializer serializer = getJsonSerializer();

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(getJsonSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        Duration fiveMinutesTtl = Duration.ofMinutes(5);

        cacheConfigurations.put("student_exam_dashboard", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("children_exam_dashboard", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("student_financial_dashboard", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("children_financial_dashboard", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("student_overall_dashboard", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("teacher_exam_dashboard", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("admin_user_overview", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("admin_exam_analytics", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("admin_revenue", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("child_exam_history", config.entryTtl(fiveMinutesTtl));
        cacheConfigurations.put("children_overview", config.entryTtl(fiveMinutesTtl));


        cacheConfigurations.put("unverified_teachers", config.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private GenericJackson2JsonRedisSerializer getJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

}