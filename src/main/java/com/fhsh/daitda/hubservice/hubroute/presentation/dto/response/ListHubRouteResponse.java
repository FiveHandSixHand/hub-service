package com.fhsh.daitda.hubservice.hubroute.presentation.dto.response;

import com.fhsh.daitda.hubservice.hubroute.application.result.ListHubRouteResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ListHubRouteResponse(
        UUID hubRouteId,
        UUID srcHubId,
        UUID destHubId,
        Integer durationTime,
        BigDecimal distance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ListHubRouteResponse from(ListHubRouteResult result) {
        return new ListHubRouteResponse(
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
