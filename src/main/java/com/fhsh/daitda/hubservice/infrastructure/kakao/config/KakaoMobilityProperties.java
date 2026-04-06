package com.fhsh.daitda.hubservice.infrastructure.kakao.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.mobility")
public record KakaoMobilityProperties(
        @NotBlank String restApiKey,
        @NotBlank String directionsBaseUrl
) {
}