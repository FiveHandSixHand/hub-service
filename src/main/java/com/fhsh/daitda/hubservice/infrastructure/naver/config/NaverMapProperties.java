package com.fhsh.daitda.hubservice.infrastructure.naver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 중앙 config에 정의된 naver.map 설정을 바인딩하는 properties
 * <p>
 * 현재 중앙관리 구조:
 * - naver.map.client-id
 * - naver.map.client-secret
 * - naver.map.direction-base-url
 * <p>
 * 왜 record로 두는가:
 * - 설정값 보관용 객체라 불변 구조가 더 적합하기 때문
 * - getter/생성자 코드를 줄일 수 있기 때문
 */
@ConfigurationProperties(prefix = "naver.map")
public record NaverMapProperties(
        String clientId,
        String clientSecret,
        String directionBaseUrl
) {
}
