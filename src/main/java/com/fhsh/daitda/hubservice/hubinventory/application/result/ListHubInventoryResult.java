package com.fhsh.daitda.hubservice.hubinventory.application.result;

import com.fhsh.daitda.hubservice.hubinventory.domain.entity.HubInventory;

import java.util.UUID;

public record ListHubInventoryResult(
        UUID hubInventoryId,
        UUID hubId,
        UUID companyId,
        UUID productId,
        Integer quantity
) {
    public static ListHubInventoryResult from(HubInventory hubInventory) {
        return new ListHubInventoryResult(
                hubInventory.getHubInventoryId(),
                hubInventory.getHubId(),
                hubInventory.getCompanyId(),
                hubInventory.getProductId(),
                hubInventory.getQuantity()
        );
    }
}
