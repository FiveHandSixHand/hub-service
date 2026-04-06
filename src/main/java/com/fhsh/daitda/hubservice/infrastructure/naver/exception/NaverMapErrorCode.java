package com.fhsh.daitda.hubservice.infrastructure.naver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum NaverMapErrorCode{

    NAVER_DIRECTIONS_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "네이버 길찾기 응답이 비어 있습니다."),
    NAVER_DIRECTIONS_ROUTE_NOT_FOUND(HttpStatus.BAD_GATEWAY, "네이버 길찾기 경로를 찾을 수 없습니다."),
    NAVER_DIRECTIONS_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "네이버 길찾기 응답 형식이 올바르지 않습니다."),
    NAVER_DIRECTIONS_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "네이버 길찾기 요청에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    NaverMapErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
