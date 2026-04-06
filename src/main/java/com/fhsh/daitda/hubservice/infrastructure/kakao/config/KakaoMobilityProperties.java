package com.fhsh.daitda.hubservice.infrastructure.kakao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.mobility")
public record KakaoMobilityProperties(
        String restApiKey,
        String directionsBaseUrl
) {
}