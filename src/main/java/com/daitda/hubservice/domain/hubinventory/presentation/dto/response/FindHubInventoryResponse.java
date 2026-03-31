package com.daitda.hubservice.domain.hubinventory.presentation.dto.response;

import com.daitda.hubservice.domain.hubinventory.domain.entity.HubInventory;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class FindHubInventoryResponse {

    private UUID hubInventoryId;
    private UUID hubId;
    private UUID companyId;
    private UUID productId;
    private Integer quantity;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환
    public static FindHubInventoryResponse from(HubInventory hubInventory) {
        return FindHubInventoryResponse.builder()
                .hubInventoryId(hubInventory.getHubInventoryId())
                .hubId(hubInventory.getHubId())
                .companyId(hubInventory.getCompanyId())
                .productId(hubInventory.getProductId())
                .quantity(hubInventory.getQuantity())
                .createdAt(hubInventory.getCreatedAt())
                .updatedAt(hubInventory.getUpdatedAt())
                .build();
    }
}
