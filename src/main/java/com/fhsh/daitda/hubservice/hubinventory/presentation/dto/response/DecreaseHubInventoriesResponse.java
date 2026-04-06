package com.fhsh.daitda.hubservice.hubinventory.presentation.dto.response;

import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DecreaseHubInventoriesResponse {

    private List<Item> items;

    public static DecreaseHubInventoriesResponse from(List<FindHubInventoryResult> results) {
        return DecreaseHubInventoriesResponse.builder()
                .items(results.stream()
                        .map(findHubInventoryResult -> Item.builder()
                                .hubInventoryId(findHubInventoryResult.hubInventoryId())
                                .hubId(findHubInventoryResult.hubId())
                                .companyId(findHubInventoryResult.companyId())
                                .productId(findHubInventoryResult.productId())
                                .quantity(findHubInventoryResult.quantity())
                                .build())
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class Item{
        private UUID hubInventoryId;
        private UUID hubId;
        private UUID companyId;
        private UUID productId;
        private Integer quantity;
    }
}