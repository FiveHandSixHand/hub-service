package com.fhsh.daitda.hubservice.infrastructure.naver.client;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.infrastructure.naver.dto.NaverDirectionsResponse;
import com.fhsh.daitda.hubservice.infrastructure.naver.exception.NaverMapErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class NaverDirectionsClientImpl implements NaverDirectionsClient {

    private final RestClient naverDirectionsRestClient;

    public NaverDirectionsClientImpl(RestClient naverDirectionsRestClient) {
        this.naverDirectionsRestClient = naverDirectionsRestClient;
    }


    @Override
    public RouteMetrics getDrivingMetrics(BigDecimal startLongitude, BigDecimal startLatitude, BigDecimal goalLongitude, BigDecimal goalLatitude) {

        // Naver Directions 5는 "경도,위도" 문자열 형식으로 start/goal을 받음
        String start = startLongitude + "," + startLatitude;
        String goal = goalLongitude + "," + goalLatitude;

        try {
            NaverDirectionsResponse response = naverDirectionsRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/driving")
                            .queryParam("start", start)
                            .queryParam("goal", goal)
                            .build())
                    .retrieve()
                    .body(NaverDirectionsResponse.class);

            if (response == null || response.route() == null || response.route().isEmpty()) {
                throw new BusinessException(NaverMapErrorCode.NAVER_DIRECTIONS_RESPONSE_EMPTY);
            }

            List<NaverDirectionsResponse.Route> routes = response.route().get("traoptimal");
            if (routes == null || routes.isEmpty()) {
                throw new BusinessException(NaverMapErrorCode.NAVER_DIRECTIONS_ROUTE_NOT_FOUND);
            }

            NaverDirectionsResponse.Summary summary = routes.get(0).summary();
            if (summary == null || summary.distance() == null || summary.duration() == null) {
                throw new BusinessException(NaverMapErrorCode.NAVER_DIRECTIONS_INVALID_RESPONSE);
            }

            int durationMinutes = Math.max(1, summary.duration() / 1000 / 60);

            BigDecimal distanceKilometers = BigDecimal.valueOf(summary.distance())
                    .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);

            return new RouteMetrics(durationMinutes, distanceKilometers);
        } catch (BusinessException e) {
            throw e;
        } catch (RestClientException e) {
            throw new BusinessException(NaverMapErrorCode.NAVER_DIRECTIONS_REQUEST_FAILED);
        }
    }
}
