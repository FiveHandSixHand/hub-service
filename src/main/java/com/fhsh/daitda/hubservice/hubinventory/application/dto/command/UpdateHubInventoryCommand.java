package com.fhsh.daitda.hubservice.hubinventory.application.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateHubInventoryCommand {

    private Integer quantity;

}
