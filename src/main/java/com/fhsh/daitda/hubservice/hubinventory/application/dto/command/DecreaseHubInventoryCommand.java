package com.fhsh.daitda.hubservice.hubinventory.application.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DecreaseHubInventoryCommand {

    private UUID hubInventoryId;
    private Integer quantity;
}
