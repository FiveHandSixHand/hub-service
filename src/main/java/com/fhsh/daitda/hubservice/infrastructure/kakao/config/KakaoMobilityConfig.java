package com.fhsh.daitda.hubservice.infrastructure.kakao.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(KakaoMobilityProperties.class)
public class KakaoMobilityConfig {

    @Bean
    public RestClient kakaoDirectionsRestClient(KakaoMobilityProperties properties) {
        log.info("Checking Kakao Directions Config -> baseUrl: [{}], restApiKeyExists: [{}]",
                properties.directionsBaseUrl(),
                properties.restApiKey() != null && !properties.restApiKey().isBlank());

        return RestClient.builder()
                .baseUrl(properties.directionsBaseUrl())
                .defaultHeader("Authorization", "KakaoAK " + properties.restApiKey())
                .build();
    }
}