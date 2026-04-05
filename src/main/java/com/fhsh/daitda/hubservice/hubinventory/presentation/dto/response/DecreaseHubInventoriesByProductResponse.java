package com.fhsh.daitda.hubservice.hubinventory.presentation.dto.response;

import com.fhsh.daitda.hubservice.hubinventory.application.result.DecreaseHubInventoriesByProductResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

// 주문 생성용 재고 차감 응답 DTO
@Getter
@Builder
public class DecreaseHubInventoriesByProductResponse {

    private List<Item> items;

    public static DecreaseHubInventoriesByProductResponse from(DecreaseHubInventoriesByProductResult result) {
        return DecreaseHubInventoriesByProductResponse.builder()
                .items(result.getItems().stream()
                        .map(item -> Item.builder()
                                .hubInventoryId(item.getHubInventoryId())
                                .productId(item.getProductId())
                                .build())
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class Item {
        private UUID hubInventoryId;
        private UUID productId;
    }
}
