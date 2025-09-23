package com.fa25se225.capstone.configuration.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String signerKey;
    private long validDurationInSecond;
    private long refreshableDurationInSecond;
    private String issuer;
}
