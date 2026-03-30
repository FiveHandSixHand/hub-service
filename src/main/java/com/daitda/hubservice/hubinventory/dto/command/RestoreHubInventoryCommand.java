package com.daitda.hubservice.hubinventory.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RestoreHubInventoryCommand {

    private UUID hubInventoryId;
    private Integer quantity;
}
