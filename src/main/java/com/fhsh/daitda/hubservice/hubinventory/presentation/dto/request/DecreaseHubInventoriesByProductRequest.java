package com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request;

import com.fhsh.daitda.hubservice.hubinventory.application.command.DecreaseHubInventoriesByProductCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class DecreaseHubInventoriesByProductRequest {

    // 공급 업체 회사 ID
    @NotNull(message = "공급 업체 ID는 필수입니다.")
    private UUID supplierCompanyId;

    // 주문 항목 목록
    @Valid
    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    private List<Item> orderItems;

    public DecreaseHubInventoriesByProductCommand toCommand() {
        return DecreaseHubInventoriesByProductCommand.builder()
                .supplierCompanyId(supplierCompanyId)
                .orderItems(orderItems.stream()
                        .map(item -> DecreaseHubInventoriesByProductCommand.Item.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
    }
    @Getter
    public static class Item {

        @NotNull(message = "상품 ID는 필수입니다.")
        private UUID productId;

        @NotNull(message = "차감 수량은 필수입니다.")
        @Min(value = 1, message = "차감 수량은 1 이상이어야 합니다.")
        private Integer quantity;
    }
}
