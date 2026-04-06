package com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request;

import com.fhsh.daitda.hubservice.hubinventory.application.command.RestoreHubInventoriesCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class RestoreHubInventoryRequest {

    @Valid
    @NotEmpty(message = "복원 항목은 최소 1개 이상이어야 합니다.")
    private List<@NotNull(message = "복원 항목에 null 요소를 포함할 수 없습니다.") @Valid Item> orderItems;

    public RestoreHubInventoriesCommand toCommand() {
        return RestoreHubInventoriesCommand.builder()
                .orderItems(orderItems.stream()
                        .map(item -> RestoreHubInventoriesCommand.Item.builder()
                                .hubInventoryId(item.getHubInventoryId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
    }

    // 개별 복원 항목
    @Getter
    public static class Item{

        @NotNull(message = "허브 재고 ID는 필수입니다.")
        private UUID hubInventoryId;

        @NotNull(message = "복원 수량은 필수입니다.")
        @Min(value = 1, message = "복원 수량은 1 이상이어야 합니다.")
        private Integer quantity;

    }
}
