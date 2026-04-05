package com.fhsh.daitda.hubservice.hubinventory.application.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DecreaseHubInventoriesCommand {

    private List<Item> items;

    @Getter
    @Builder
    public static class Item{
        private UUID hubInventoryId;
        private Integer quantity;
    }
}
