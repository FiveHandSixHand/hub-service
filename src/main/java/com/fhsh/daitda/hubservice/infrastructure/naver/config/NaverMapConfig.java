package com.fhsh.daitda.hubservice.infrastructure.naver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// Naver Maps REST API 호출용 RestClient 설정
@Configuration
public class NaverMapConfig {

    @Bean
    public RestClient naverDirectionsRestClient(NaverMapProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.directionBaseUrl())
                .defaultHeader("x-ncp-apigw-api-key-id", properties.clientId())
                .defaultHeader("x-ncp-apigw-api-key", properties.clientSecret())
                .build();
    }
}
