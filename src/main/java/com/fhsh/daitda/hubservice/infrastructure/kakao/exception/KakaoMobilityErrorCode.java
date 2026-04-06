package com.fhsh.daitda.hubservice.infrastructure.kakao.exception;

import com.fhsh.daitda.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum KakaoMobilityErrorCode implements ErrorCode {

    KAKAO_DIRECTIONS_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "카카오 길찾기 응답이 비어 있습니다."),
    KAKAO_DIRECTIONS_ROUTE_NOT_FOUND(HttpStatus.BAD_GATEWAY, "카카오 길찾기 경로를 찾을 수 없습니다."),
    KAKAO_DIRECTIONS_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "카카오 길찾기 응답 형식이 올바르지 않습니다."),
    KAKAO_DIRECTIONS_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 길찾기 요청에 실패했습니다.");

    private final HttpStatus status;
    private final String description;

    KakaoMobilityErrorCode(HttpStatus status, String description) {
        this.status = status;
        this.description = description;
    }
}