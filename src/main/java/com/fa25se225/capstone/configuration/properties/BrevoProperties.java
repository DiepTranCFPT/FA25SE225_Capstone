package com.fa25se225.capstone.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "brevo")
public class BrevoProperties {
    private String apiKey;
    private Sender sender;
    private Map<String, Long> templates;

    @Getter
    @Setter
    public static class Sender {
        private String name;
        private String email;
    }
}