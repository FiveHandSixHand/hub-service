package com.fhsh.daitda.hubservice.hubroute.presentation.dto.request;

import com.fhsh.daitda.hubservice.hubroute.application.command.UpdateHubRouteCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class UpdateHubRouteRequest {

    public UpdateHubRouteCommand toCommand() {
        return UpdateHubRouteCommand.builder().build();
    }
}
