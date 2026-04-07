package com.fhsh.daitda.hubservice.hubroute.application.result;

import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;

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
        String durationText,
        BigDecimal distance,
        String distanceText
) {
    public static FindHubRoutePathResult of(
            Integer sequence,
            UUID hubRouteId,
            Hub srcHub,
            Hub destHub,
            Integer durationTime,
            BigDecimal distance
    ) {
        return new FindHubRoutePathResult(
                sequence,
                hubRouteId,
                srcHub.getHubId(),
                srcHub.getHubName(),
                srcHub.getHubAddress(),
                destHub.getHubId(),
                destHub.getHubName(),
                destHub.getHubAddress(),
                durationTime,
                toDurationText(durationTime),
                distance,
                toDistanceText(distance)
        );
    }

    private static String toDurationText(Integer durationTime) {
        int hours = durationTime / 60;
        int minutes = durationTime % 60;

        if (hours == 0) {
            return minutes + "분";
        }
        if (minutes == 0) {
            return hours + "시간";
        }
        return hours + "시간 " + minutes + "분";
    }

    private static String toDistanceText(BigDecimal distance) {
        return distance.setScale(2, RoundingMode.HALF_UP).toPlainString() + "km";
    }
}