package com.fhsh.daitda.hubservice.hubroute.application.result;

import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record FindHubRoutePathResult(
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
    public static FindHubRoutePathResult from(Integer sequence, HubRoute hubRoute, Hub srcHub, Hub destHub) {
        return new FindHubRoutePathResult(
                sequence,
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
                toDistanceKm(hubRoute.getDistance())
        );
    }

    private static String toDurationMinutes(Integer durationTime) {
        return durationTime + "분";
    }

    private static String toDistanceKm(BigDecimal distance) {
        return distance.setScale(2, RoundingMode.HALF_UP).toPlainString() + "km";
    }
}