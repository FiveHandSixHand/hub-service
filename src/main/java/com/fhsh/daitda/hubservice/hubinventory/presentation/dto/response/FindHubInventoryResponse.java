package com.fhsh.daitda.hubservice.hubinventory.presentation.dto.response;

import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
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
    public static FindHubInventoryResponse from(FindHubInventoryResult result) {
        return FindHubInventoryResponse.builder()
                .hubInventoryId(result.hubInventoryId())
                .hubId(result.hubId())
                .companyId(result.companyId())
                .productId(result.productId())
                .quantity(result.quantity())
                .createdAt(result.createdAt())
                .updatedAt(result.updatedAt())
                .build();
    }
}
