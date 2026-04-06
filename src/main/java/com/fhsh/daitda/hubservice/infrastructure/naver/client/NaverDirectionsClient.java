package com.fhsh.daitda.hubservice.infrastructure.naver.client;

import java.math.BigDecimal;

// hub-route 생성 시 Naver Directions를 통해 거리/시간을 계산하기 위한 외부 클라이언트 인터페이스
public interface NaverDirectionsClient {

    RouteMetrics getDrivingMetrics(
            BigDecimal startLongitude,
            BigDecimal startLatitude,
            BigDecimal goalLongitude,
            BigDecimal goalLatitude
    );


    /**
     * 도메인에서 실제로 저장할 최소 계산 결과
     *
     * durationMinutes:
     * - 분 단위 소요시간
     *
     * distanceKilometers:
     * - km 단위 거리
     */
    record RouteMetrics(
            Integer durationMinutes,
            BigDecimal distanceKilometers
    ) {
    }
}
