package com.fhsh.daitda.hubservice.infrastructure.kakao.client;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.infrastructure.kakao.dto.KakaoDirectionsResponse;
import com.fhsh.daitda.hubservice.infrastructure.kakao.exception.KakaoMobilityErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
public class KakaoDirectionsClientImpl implements KakaoDirectionsClient {

    private final RestClient kakaoDirectionsRestClient;

    public KakaoDirectionsClientImpl(RestClient kakaoDirectionsRestClient) {
        this.kakaoDirectionsRestClient = kakaoDirectionsRestClient;
    }

    @Override
    public RouteMetrics getDrivingMetrics(
            BigDecimal startLongitude,
            BigDecimal startLatitude,
            BigDecimal goalLongitude,
            BigDecimal goalLatitude
    ) {
        // Kakao Mobility 길찾기 API는 "경도,위도" 문자열 형식으로 origin/destination을 받음
        String origin = startLongitude + "," + startLatitude;
        String destination = goalLongitude + "," + goalLatitude;

        try {
            KakaoDirectionsResponse response = kakaoDirectionsRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/directions")
                            .queryParam("origin", origin)
                            .queryParam("destination", destination)
                            .queryParam("priority", "RECOMMEND")
                            .build())
                    .retrieve()
                    .body(KakaoDirectionsResponse.class);

            if (response == null || response.routes() == null || response.routes().isEmpty()) {
                throw new BusinessException(KakaoMobilityErrorCode.KAKAO_DIRECTIONS_RESPONSE_EMPTY);
            }

            List<KakaoDirectionsResponse.Route> routes = response.routes();
            KakaoDirectionsResponse.Route route = routes.get(0);

            if (route.resultCode() == null || route.resultMsg() == null) {
                throw new BusinessException(KakaoMobilityErrorCode.KAKAO_DIRECTIONS_INVALID_RESPONSE);
            }

            if (route.resultCode() != 0) {
                log.warn("Kakao directions route not found. origin={}, destination={}, resultCode={}, resultMsg={}",
                        origin, destination, route.resultCode(), route.resultMsg());
                throw new BusinessException(KakaoMobilityErrorCode.KAKAO_DIRECTIONS_ROUTE_NOT_FOUND);
            }

            KakaoDirectionsResponse.Summary summary = route.summary();
            if (summary == null || summary.distance() == null || summary.duration() == null) {
                throw new BusinessException(KakaoMobilityErrorCode.KAKAO_DIRECTIONS_INVALID_RESPONSE);
            }

            int durationMinutes = Math.max(1, (summary.duration() + 59) / 60);

            long distanceMeters = summary.distance().longValue();
            BigDecimal distanceKilometers = BigDecimal.valueOf(distanceMeters)
                    .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);

            return new RouteMetrics(durationMinutes, distanceMeters, distanceKilometers);

        } catch (BusinessException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("Kakao directions request failed. origin={}, destination={}", origin, destination, e);
            throw new BusinessException(KakaoMobilityErrorCode.KAKAO_DIRECTIONS_REQUEST_FAILED);
        }
    }
}