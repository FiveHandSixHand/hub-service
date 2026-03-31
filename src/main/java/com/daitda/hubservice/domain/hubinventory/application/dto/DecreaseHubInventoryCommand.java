package com.daitda.hubservice.domain.hubinventory.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DecreaseHubInventoryCommand {

    private UUID hubInventoryId;
    private Integer quantity;
}
