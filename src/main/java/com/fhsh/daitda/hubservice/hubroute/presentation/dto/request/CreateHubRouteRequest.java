package com.fhsh.daitda.hubservice.hubroute.presentation.dto.request;

import com.fhsh.daitda.hubservice.hubroute.application.command.CreateHubRouteCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateHubRouteRequest {

    @NotNull(message = "출발 허브 ID는 필수입니다.")
    private UUID srcHubId;

    @NotNull(message = "도착 허브 ID는 필수입니다.")
    private UUID destHubId;

    public CreateHubRouteCommand toCommand() {
        return CreateHubRouteCommand.builder()
                .srcHubId(srcHubId)
                .destHubId(destHubId)
                .build();
    }
}
