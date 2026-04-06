package com.fhsh.daitda.hubservice.infrastructure.naver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 중앙관리 config의 naver.map 설정을 바인딩하는 객체
 *
 * 현재 중앙관리 구조:
 * - naver.map.client-id
 * - naver.map.client-secret
 * - naver.map.direction-base-url
 * - naver.map.geocode-base-url
 */
@ConfigurationProperties(prefix = "naver.map")
public record NaverMapProperties(
        String clientId,
        String clientSecret,
        String directionBaseUrl,
        String geocodeBaseUrl
) {
}
