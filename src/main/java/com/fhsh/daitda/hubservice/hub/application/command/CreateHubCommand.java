package com.fhsh.daitda.hubservice.hub.application.command;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CreateHubCommand {

    private String hubName;
    private String hubAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isCentral;
}
