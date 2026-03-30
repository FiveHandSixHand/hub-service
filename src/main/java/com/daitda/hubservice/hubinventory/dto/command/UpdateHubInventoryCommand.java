package com.daitda.hubservice.hubinventory.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateHubInventoryCommand {

    private Integer quantity;

}
