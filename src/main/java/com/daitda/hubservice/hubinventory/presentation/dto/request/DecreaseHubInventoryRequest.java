package com.daitda.hubservice.hubinventory.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DecreaseHubInventoryRequest {

    @NotNull(message = "재고 ID는 필수입니다.")
    private UUID hubInventoryId;

    @NotNull(message = "차감 수량은 필수압니다.")
    @Min(value = 1, message = "차감 수량은 1 이상이어야 합니다.")
    private Integer quantity;
}
