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

    @NotNull(message = "소요 시간은 필수입니다.")
    @Positive(message = "소요 시간은 0보다 커야 합니다.")
    private Integer durationTime;

    @NotNull(message = "이동 거리는 필수입니다.")
    @Positive(message = "이동 거리는 0보다 커야 합니다.")
    private BigDecimal distance;

    public CreateHubRouteCommand toCommand() {
        return CreateHubRouteCommand.builder()
                .srcHubId(srcHubId)
                .destHubId(destHubId)
                .durationTime(durationTime)
                .distance(distance)
                .build();
    }
}
