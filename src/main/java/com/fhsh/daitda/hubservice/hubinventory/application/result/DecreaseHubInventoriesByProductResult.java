package com.fhsh.daitda.hubservice.hubinventory.application.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DecreaseHubInventoriesByProductResult {

    // 상품별 실제 차감에 사용된 재고 row 정보
    private List<Item> items;

    @Getter
    @Builder
    public static class Item{
        // 실제 차감에 사용한 재고 row PK
        private UUID hubInventoryId;
        // 어떤 상품에 대한 결과인지 식별하기 위해 포함
        private UUID productId;
    }
}
