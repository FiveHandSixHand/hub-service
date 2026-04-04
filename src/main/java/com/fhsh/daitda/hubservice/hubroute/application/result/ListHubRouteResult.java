package com.fhsh.daitda.hubservice.hubroute.application.result;

import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ListHubRouteResult(
        UUID hubRouteId,
        UUID srcHubId,
        UUID destHubId,
        Integer durationTime,
        BigDecimal distance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ListHubRouteResult from(HubRoute hubRoute) {
        return new ListHubRouteResult(
                hubRoute.getHubRouteId(),
                hubRoute.getSrcHubId(),
                hubRoute.getDestHubId(),
                hubRoute.getDurationTime(),
                hubRoute.getDistance(),
                hubRoute.getCreatedAt(),
                hubRoute.getUpdatedAt()
        );
    }
}
