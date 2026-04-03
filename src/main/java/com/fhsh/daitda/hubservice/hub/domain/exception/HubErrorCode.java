package com.fhsh.daitda.hubservice.hub.domain.exception;

import com.fhsh.daitda.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum HubErrorCode implements ErrorCode {

    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "허브를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String description;

    HubErrorCode(HttpStatus status, String description) {
        this.status = status;
        this.description = description;
    }
}
