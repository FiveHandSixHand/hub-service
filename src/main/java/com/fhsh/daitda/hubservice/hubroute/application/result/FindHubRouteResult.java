package com.fhsh.daitda.hubservice.hubroute.application.result;

import com.fhsh.daitda.hubservice.hubroute.doamin.entity.HubRoute;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FindHubRouteResult(
        UUID hubRouteId,
        UUID srcHubId,
        UUID destHubId,
        Integer durationTime,
        BigDecimal distance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FindHubRouteResult from(HubRoute hubRoute) {
        return new FindHubRouteResult(
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
