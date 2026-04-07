package com.fhsh.daitda.hubservice.hubroute.presentation.dto.response;

import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRoutePathResult;

import java.math.BigDecimal;
import java.util.UUID;

public record FindHubRoutePathResponse(
        Integer sequence,
        UUID hubRouteId,
        UUID srcHubId,
        String srcHubName,
        String srcHubAddress,
        UUID destHubId,
        String destHubName,
        String destHubAddress,
        Integer durationTime,
        String durationMinutes,
        BigDecimal distance,
        String distanceKm
) {
    public static FindHubRoutePathResponse from(FindHubRoutePathResult result) {
        return new FindHubRoutePathResponse(
                result.sequence(),
                result.hubRouteId(),
                result.srcHubId(),
                result.srcHubName(),
                result.srcHubAddress(),
                result.destHubId(),
                result.destHubName(),
                result.destHubAddress(),
                result.durationTime(),
                result.durationMinutes(),
                result.distance(),
                result.distanceKm()
        );
    }
}