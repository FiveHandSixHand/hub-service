package com.daitda.hubservice.hubinventory.presentation.dto.response;

import com.daitda.hubservice.hubinventory.application.result.ListHubInventoryResult;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ListHubInventoryResponse {

    private UUID hubInventoryId;
    private UUID hubId;
    private UUID companyId;
    private UUID productId;
    private Integer quantity;

    // 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환
    public static ListHubInventoryResponse from(ListHubInventoryResult result) {
        return ListHubInventoryResponse.builder()
                .hubInventoryId(result.hubInventoryId())
                .hubId(result.hubId())
                .companyId(result.companyId())
                .productId(result.productId())
                .quantity(result.quantity())
                .build();
    }
}
