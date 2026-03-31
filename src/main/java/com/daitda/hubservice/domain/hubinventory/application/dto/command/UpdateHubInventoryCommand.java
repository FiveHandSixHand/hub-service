package com.daitda.hubservice.domain.hubinventory.application.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateHubInventoryCommand {

    private Integer quantity;

}
