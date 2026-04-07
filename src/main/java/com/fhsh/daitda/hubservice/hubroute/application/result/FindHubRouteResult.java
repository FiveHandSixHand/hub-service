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
        String durationText,
        BigDecimal distance,
        String distanceText,
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
                toDurationText(hubRoute.getDurationTime()),
                hubRoute.getDistance(),
                toDistanceText(hubRoute.getDistance()),
                hubRoute.getCreatedAt(),
                hubRoute.getUpdatedAt()
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