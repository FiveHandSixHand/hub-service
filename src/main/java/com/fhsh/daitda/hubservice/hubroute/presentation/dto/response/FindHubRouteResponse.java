package com.fhsh.daitda.hubservice.hubroute.presentation.dto.response;

import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FindHubRouteResponse(
        UUID hubRouteId,
        UUID srcHubId,
        String srcHubName,
        String srcHubAddress,
        UUID destHubId,
        String destHubName,
        String destHubAddress,
        Integer durationTime,
        String durationText,
        BigDecimal distance,
        String distanceText,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FindHubRouteResponse from(FindHubRouteResult result) {
        return new FindHubRouteResponse(
                result.hubRouteId(),
                result.srcHubId(),
                result.srcHubName(),
                result.srcHubAddress(),
                result.destHubId(),
                result.destHubName(),
                result.destHubAddress(),
                result.durationTime(),
                result.durationText(),
                result.distance(),
                result.distanceText(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}