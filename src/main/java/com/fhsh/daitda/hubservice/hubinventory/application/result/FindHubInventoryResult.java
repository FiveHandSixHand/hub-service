package com.fhsh.daitda.hubservice.hubinventory.application.result;

import com.fhsh.daitda.hubservice.hubinventory.domain.entity.HubInventory;

import java.time.LocalDateTime;
import java.util.UUID;

public record FindHubInventoryResult(
        UUID hubInventoryId,
        UUID hubId,
        UUID companyId,
        UUID productId,
        Integer quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FindHubInventoryResult from(HubInventory hubInventory) {
        return new FindHubInventoryResult(
                hubInventory.getHubInventoryId(),
                hubInventory.getHubId(),
                hubInventory.getCompanyId(),
                hubInventory.getProductId(),
                hubInventory.getQuantity(),
                hubInventory.getCreatedAt(),
                hubInventory.getUpdatedAt()
        );
    }
}
