package com.daitda.hubservice.domain.hubinventory.application.result;

import com.daitda.hubservice.domain.hubinventory.domain.entity.HubInventory;
import org.hibernate.annotations.processing.Find;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FindHubInventoryResult(
        UUID hubInventoryId,
        UUID hubId,
        UUID companyId,
        UUID productId,
        Integer quantity,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
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
