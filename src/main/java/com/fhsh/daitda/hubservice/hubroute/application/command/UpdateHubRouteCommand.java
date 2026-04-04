package com.fhsh.daitda.hubservice.hubroute.application.command;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class UpdateHubRouteCommand {

    private Integer durationTime;
    private BigDecimal distance;
}
