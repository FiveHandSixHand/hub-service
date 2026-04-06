package com.fhsh.daitda.hubservice.hubroute.application.result;

import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

public record FindHubRouteResult(
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
        String distanceKm,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static FindHubRouteResult from(HubRoute hubRoute, Hub srcHub, Hub destHub) {
        return new FindHubRouteResult(
                hubRoute.getHubRouteId(),
                hubRoute.getSrcHubId(),
                srcHub.getHubName(),
                srcHub.getHubAddress(),
                hubRoute.getDestHubId(),
                destHub.getHubName(),
                destHub.getHubAddress(),
                hubRoute.getDurationTime(),
                toDurationMinutes(hubRoute.getDurationTime()),
                hubRoute.getDistance(),
                toDistanceKm(hubRoute.getDistance()),
                hubRoute.getCreatedAt(),
                hubRoute.getUpdatedAt()
        );
    }

    private static String toDurationMinutes(Integer durationTime) {
        return durationTime + "분";
    }

    private static String toDistanceKm(BigDecimal distance) {
        return distance.setScale(2, RoundingMode.HALF_UP).toPlainString() + "km";
    }
}