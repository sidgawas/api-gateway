package com.siddharthgawas.apigateway.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application properties for the API Gateway.
 * <p>
 * This class is used to bind properties prefixed with "application" from the
 * application configuration file (e.g., application.yml or application.properties).
 */
@ConfigurationProperties(prefix = "application")
@Setter
@Getter
@Configuration
public class ApplicationProperties {
    private String secret;
    private Long jwtAccessTokenExpirationMs;
    private Long jwtRefreshTokenExpirationMs;
    private String jwtIssuer;
    private Long maxReqPerMinute;
}
