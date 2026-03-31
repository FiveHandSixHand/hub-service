package com.daitda.hubservice.domain.hubinventory.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CreateHubInventoryCommand {

    private UUID hubId;
    private UUID companyId;
    private UUID productId;
    private Integer quantity;

}
