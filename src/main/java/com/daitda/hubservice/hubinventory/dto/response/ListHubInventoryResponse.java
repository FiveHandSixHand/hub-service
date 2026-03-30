package com.daitda.hubservice.hubinventory.dto.response;

import com.daitda.hubservice.hubinventory.entity.HubInventory;
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
    public static ListHubInventoryResponse from(HubInventory hubInventory) {
        return ListHubInventoryResponse.builder()
                .hubInventoryId(hubInventory.getHubInventoryId())
                .hubId(hubInventory.getHubId())
                .companyId(hubInventory.getCompanyId())
                .productId(hubInventory.getProductId())
                .quantity(hubInventory.getQuantity())
                .build();
    }
}
