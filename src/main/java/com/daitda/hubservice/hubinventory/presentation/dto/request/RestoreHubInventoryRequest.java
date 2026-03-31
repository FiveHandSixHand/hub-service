package com.daitda.hubservice.hubinventory.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RestoreHubInventoryRequest {

    @NotNull(message = "허브 재고 ID는 필수입니다.")
    private UUID hubInventoryID;

    @NotNull(message = "복원 수량은 필수입니다.")
    @Min(value = 1, message = "복원 수량은 1 이상이어야 합니다.")
    private Integer quantity;
}
