package com.daitda.hubservice.hubinventory.dto.request;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateHubInventoryRequest {

    @NotNull(message = "허브 ID는 필수입니다.")
    private UUID hubId;

    @NotNull(message = "업체 ID는 필수입니다.")
    private UUID companyId;

    @NotNull(message = "상품 ID는 필수입니다.")
    private UUID productId;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private Integer quantity;
}
