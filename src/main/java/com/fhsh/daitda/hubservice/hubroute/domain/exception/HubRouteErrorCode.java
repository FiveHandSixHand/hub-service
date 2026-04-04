package com.fhsh.daitda.hubservice.hubroute.domain.exception;

import com.fhsh.daitda.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum HubRouteErrorCode implements ErrorCode {

    HUB_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "허브 경로를 찾을 수 없습니다."),
    HUB_ROUTE_CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 허브 경로입니다.");

    private final HttpStatus status;
    private final String description;

    HubRouteErrorCode(HttpStatus status, String description) {
        this.status = status;
        this.description = description;
    }
}
