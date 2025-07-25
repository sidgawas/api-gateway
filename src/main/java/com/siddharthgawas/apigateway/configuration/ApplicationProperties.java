package com.siddharthgawas.apigateway.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "application")
@Setter
@Getter
@Configuration
public class ApplicationProperties {
    private String secret;
    private Long jwtAccessTokenExpirationMs;
    private Long jwtRefreshTokenExpirationMs;
    private String jwtIssuer;
}
