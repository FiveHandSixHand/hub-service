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

    @NotNull(message = "소요 시간은 필수입니다.")
    @Positive(message = "소요 시간은 0보다 커야 합니다.")
    private Integer durationTime;

    @NotNull(message = "이동 거리는 필수입니다.")
    @Positive(message = "이동 거리는 0보다 커야 합니다.")
    private BigDecimal distance;

    public UpdateHubRouteCommand toCommand() {
        return UpdateHubRouteCommand.builder()
                .durationTime(durationTime)
                .distance(distance)
                .build();
    }
}
