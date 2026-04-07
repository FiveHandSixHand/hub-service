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

    /**
     * 분 단위 소요시간을 사람이 읽기 쉬운 문자열로 변환
     * - 60분 이하: "N분"
     * - 60분 초과: "N시간 M분"
     * - 정각 시간: "N시간"
     */
    private static String toDurationText(Integer durationTime) {
        // 소요시간이 60분 이하 일시 "분" 표시 추가
        if (durationTime <= 60) {
            return durationTime + "분";
        }

        int hours = durationTime / 60;
        int minutes = durationTime % 60;

        if (minutes == 0) {
            return hours + "시간";
        }
        return hours + "시간 " + minutes + "분";
    }

    private static String toDistanceText(BigDecimal distance) {
        return distance.setScale(2, RoundingMode.HALF_UP).toPlainString() + "km";
    }
}