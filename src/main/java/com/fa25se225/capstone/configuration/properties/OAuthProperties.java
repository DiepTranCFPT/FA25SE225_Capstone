package com.fa25se225.capstone.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "outbound.oauth2")
public class OAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String grantType;

}
