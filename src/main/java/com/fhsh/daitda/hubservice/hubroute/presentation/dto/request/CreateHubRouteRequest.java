package com.fhsh.daitda.hubservice.hubroute.presentation.dto.request;

import com.fhsh.daitda.hubservice.hubroute.application.command.CreateHubRouteCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
