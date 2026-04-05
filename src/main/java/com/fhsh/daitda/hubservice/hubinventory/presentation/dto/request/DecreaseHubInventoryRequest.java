package com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request;

import com.fhsh.daitda.hubservice.hubinventory.application.command.DecreaseHubInventoriesCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class DecreaseHubInventoryRequest {

    @Valid
    @NotEmpty(message = "차감 항목은 최소 1개 이상이어야 합니다.")
    private List<Item> items;

    public DecreaseHubInventoriesCommand toCommand() {
        return DecreaseHubInventoriesCommand.builder()
                .items(items.stream()
                        .map(item -> DecreaseHubInventoriesCommand.Item.builder()
                                .hubInventoryId(item.getHubInventoryId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
    }

    @Getter
    public static class Item {

        @NotNull(message = "재고 ID는 필수입니다.")
        private UUID hubInventoryId;

        @NotNull(message = "차감 수량은 필수입니다.")
        @Min(value = 1, message = "차감 수량은 1 이상이어야 합니다.")
        private Integer quantity;
    }
}