package com.fhsh.daitda.hubservice.hubroute.presentation.dto.response;

import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FindHubRouteResponse(
        UUID hubRouteId,
        UUID srcHubId,
        UUID destHubId,
        Integer durationTIme,
        BigDecimal distance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FindHubRouteResponse from(FindHubRouteResult result) {
        return new FindHubRouteResponse(
                result.hubRouteId(),
                result.srcHubId(),
                result.destHubId(),
                result.durationTime(),
                result.distance(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
