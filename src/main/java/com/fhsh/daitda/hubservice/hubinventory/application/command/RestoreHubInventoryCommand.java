package com.fhsh.daitda.hubservice.hubinventory.application.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RestoreHubInventoryCommand {

    // 복원 대상 목록
    private List<Item> orderItems;

    // 개별 복원 항목
    @Getter
    @Builder
    public static class Item {
        private UUID hubInventoryId;
        private Integer quantity;
    }
}
