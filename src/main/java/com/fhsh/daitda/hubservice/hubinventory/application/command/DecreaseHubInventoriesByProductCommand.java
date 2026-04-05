package com.fhsh.daitda.hubservice.hubinventory.application.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

// 주문 생성용 재고 차감 command
@Getter
@Builder
public class DecreaseHubInventoriesByProductCommand {

    /**
     * 공급 업체 회사 ID
     * 한 회사는 한 허브에만 물건을 둠
     * supplierCompanyId + productId 로
     * 실제 재고 row를 찾는 데 사용
     */
    private UUID supplierCompanyId;

    /**
     * 주문 항목 목록
     */
    private List<Item> orderItems;

    // 개별 주문 항목 command
    @Getter
    @Builder
    public static class Item{
        private UUID productId;
        private Integer quantity;
    }
}
