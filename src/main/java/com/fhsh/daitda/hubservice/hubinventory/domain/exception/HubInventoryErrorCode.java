package com.fhsh.daitda.hubservice.hubinventory.domain.exception;

import com.fhsh.daitda.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum HubInventoryErrorCode implements ErrorCode {

    HUB_INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "허브 재고를 찾을 수 없습니다."),
    HUB_INVENTORY_CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 허브 재고입니다.");

    private final HttpStatus status;
    private final String description;

    HubInventoryErrorCode(HttpStatus status, String description) {
        this.status = status;
        this.description = description;
    }
}
