package com.fhsh.daitda.hubservice.hubroute.application.service.query;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.exception.HubErrorCode;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRoutePathResult;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.result.ListHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import com.fhsh.daitda.hubservice.hubroute.domain.exception.HubRouteErrorCode;
import com.fhsh.daitda.hubservice.hubroute.domain.repository.HubRouteRepository;
import com.fhsh.daitda.hubservice.infrastructure.kakao.client.KakaoDirectionsClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HubRouteQueryService {

    private static final BigDecimal RELAY_THRESHOLD_KM = new BigDecimal("200");

    private final HubRouteRepository hubRouteRepository;
    private final HubRepository hubRepository;
    private final KakaoDirectionsClient kakaoDirectionsClient;

    public HubRouteQueryService(HubRouteRepository hubRouteRepository,
                                HubRepository hubRepository,
                                KakaoDirectionsClient kakaoDirectionsClient) {
        this.hubRouteRepository = hubRouteRepository;
        this.hubRepository = hubRepository;
        this.kakaoDirectionsClient = kakaoDirectionsClient;
    }

    public List<ListHubRouteResult> getHubRoutes() {
        return hubRouteRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(ListHubRouteResult::from)
                .toList();
    }

    public FindHubRouteResult getHubRoute(UUID hubRouteId) {
        HubRoute hubRoute = findActiveHubRoute(hubRouteId);
        return toFindHubRouteResult(hubRoute);
    }

    public FindHubRouteResult searchHubRoute(UUID srcHubId, UUID destHubId) {
        HubRoute hubRoute = hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(srcHubId, destHubId)
                .orElseThrow(() -> new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND));

        return toFindHubRouteResult(hubRoute);
    }

    /**
     * 최종 배송 경로(path) 조회
     *
     * 정책:
     * - 출발 허브와 도착 허브의 직행 거리를 먼저 계산
     * - 직행 거리가 200km 미만이면 직행 1건만 반환
     * - 직행 거리가 200km 이상이면 relay hub를 1개 선정해 2구간으로 분할 반환
     *
     * 반환값:
     * - 200km 미만: sequence=1 인 1건 리스트
     * - 200km 이상: sequence=1,2 인 2건 리스트
     */
    public List<FindHubRoutePathResult> getHubRoutePath(UUID srcHubId, UUID destHubId) {
        validateDifferentHub(srcHubId, destHubId);

        Hub srcHub = findActiveHub(srcHubId);
        Hub destHub = findActiveHub(destHubId);

        KakaoDirectionsClient.RouteMetrics directMetrics = kakaoDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        );

        // 200km 미만이면 직행 1건 반환
        if (directMetrics.distanceKilometers().compareTo(RELAY_THRESHOLD_KM) < 0) {
            return List.of(
                    toPathResult(
                            1,
                            findHubRouteIdOrNull(srcHubId, destHubId),
                            srcHub,
                            destHub,
                            directMetrics.durationMinutes(),
                            directMetrics.distanceKilometers()
                    )
            );
        }

        // 200km 이상이면 relay hub 선정
        Hub relayHub = selectRelayHub(srcHub, destHub);

        KakaoDirectionsClient.RouteMetrics firstMetrics = kakaoDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                relayHub.getLongitude(),
                relayHub.getLatitude()
        );

        KakaoDirectionsClient.RouteMetrics secondMetrics = kakaoDirectionsClient.getDrivingMetrics(
                relayHub.getLongitude(),
                relayHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        );

        List<FindHubRoutePathResult> results = new ArrayList<>();
        results.add(
                toPathResult(
                        1,
                        findHubRouteIdOrNull(srcHub.getHubId(), relayHub.getHubId()),
                        srcHub,
                        relayHub,
                        firstMetrics.durationMinutes(),
                        firstMetrics.distanceKilometers()
                )
        );
        results.add(
                toPathResult(
                        2,
                        findHubRouteIdOrNull(relayHub.getHubId(), destHub.getHubId()),
                        relayHub,
                        destHub,
                        secondMetrics.durationMinutes(),
                        secondMetrics.distanceKilometers()
                )
        );

        return results;
    }

    /**
     * 200km 이상 장거리 배송 시 사용할 중간 경유 허브를 선정
     *
     * 선정 기준:
     * - 활성 허브 중 출발/도착 허브를 제외한 후보를 순회
     * - 각 후보에 대해 [출발 -> 후보], [후보 -> 도착] 거리를 카카오로 계산
     * - 두 구간 모두 200km 미만인 후보만 유효하다.
     * - 유효 후보 중 총 거리 합이 가장 짧은 허브를 relay hub로 선택
     */
    private Hub selectRelayHub(Hub srcHub, Hub destHub) {
        List<Hub> hubs = hubRepository.findAllByDeletedAtIsNull();

        // 가장 적절하다고 판단된 경유 허브
        Hub bestRelayHub = null;
        // 가장 짧은 총거리 (출발 - 경유 + 경유 - 도착)
        BigDecimal bestTotalDistance = null;

        for (Hub candidate : hubs) {
            // 출발, 도착 허브는 경유가 될 수 없음
            if (candidate.getHubId().equals(srcHub.getHubId()) || candidate.getHubId().equals(destHub.getHubId())) {
                continue;
            }

            /**
             * 출발 - 경유후보? 구간의 길찾기 결과 계산
             * 허브 경유 구간이 가능한지 판단
             */
            KakaoDirectionsClient.RouteMetrics firstMetrics = kakaoDirectionsClient.getDrivingMetrics(
                    srcHub.getLongitude(),
                    srcHub.getLatitude(),
                    candidate.getLongitude(),
                    candidate.getLatitude()
            );


            // 경유 - 도착 구간의 길찾기 결과 계산
            KakaoDirectionsClient.RouteMetrics secondMetrics = kakaoDirectionsClient.getDrivingMetrics(
                    candidate.getLongitude(),
                    candidate.getLatitude(),
                    destHub.getLongitude(),
                    destHub.getLatitude()
            );

            // 두 구간 모두 200km 미만이어야 유효한 후보
            if (firstMetrics.distanceKilometers().compareTo(RELAY_THRESHOLD_KM) >= 0) {
                continue;
            }
            if (secondMetrics.distanceKilometers().compareTo(RELAY_THRESHOLD_KM) >= 0) {
                continue;
            }

            // 후보 허브를 경유했을 떄의 총 거리
            BigDecimal totalDistance = firstMetrics.distanceKilometers().add(secondMetrics.distanceKilometers());

            // 아직 선택된 후보가 없거나 더 좋은 후보가 있을시 갱신
            if (bestTotalDistance == null || totalDistance.compareTo(bestTotalDistance) < 0) {
                bestTotalDistance = totalDistance;
                bestRelayHub = candidate;
            }
        }

        // 끝까지 돌려도 없을시 예외
        if (bestRelayHub == null) {
            throw new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
        }

        return bestRelayHub;
    }

    /**
     * 계산된 구간과 동일한 저장된 hub_route가 있으면 해당 ID를 반환
     * 등록된 경로가 없으면 null을 반환
     *
     * path 조회는 저장된 경로 조회만을 의미하지 않음
     * 정책 기반으로 계산된 배송 구간도 응답에 포함할 수 있다
     * - 이미 등록된 구간이면 hubRouteId를 응답에 포함
     * - 아직 등록되지 않은 계산 구간이면 hubRouteId는 null반환
     *
     * hubRouteId가 null인 것은 오류가 아니라
     * (계산은 되었지만 DB에 저장된 허브 경로 row는 없음)-허브 간 이동정보 등록이 안됨 을 의미합니다
     */
    private UUID findHubRouteIdOrNull(UUID srcHubId, UUID destHubId) {

        // 출발 허브ID와 도착 허브ID가 정확히 일치하는 저장된 허브 조회
        Optional<HubRoute> hubRoute = hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(srcHubId, destHubId);

        //만약 저장된 경로가 있으면 HubRouteId를 반환 그렇지 않으면 null 반환
        return hubRoute.map(route -> route.getHubRouteId())
                .orElse(null);
    }

    /**
     * 최종 배송 경로 조회 응답용 DTO를 생성
     *
     * 숫자 원본(distance, durationTime)과 함께
     * 표시용 문자열(distanceText, durationText)도 함께 내림
     */
    private FindHubRoutePathResult toPathResult(Integer sequence,
                                                UUID hubRouteId,
                                                Hub srcHub,
                                                Hub destHub,
                                                Integer durationTime,
                                                BigDecimal distance) {
        return FindHubRoutePathResult.of(
                sequence,
                hubRouteId,
                srcHub,
                destHub,
                durationTime,
                distance
        );
    }

    private FindHubRouteResult toFindHubRouteResult(HubRoute hubRoute) {
        Hub srcHub = findActiveHub(hubRoute.getSrcHubId());
        Hub destHub = findActiveHub(hubRoute.getDestHubId());
        return FindHubRouteResult.from(hubRoute, srcHub, destHub);
    }

    private Hub findActiveHub(UUID hubId) {
        return hubRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new BusinessException(HubErrorCode.HUB_NOT_FOUND));
    }

    private void validateDifferentHub(UUID srcHubId, UUID destHubId) {
        if (srcHubId == null || destHubId == null) {
            throw new IllegalArgumentException("출발 허브와 도착 허브는 필수입니다.");
        }

        if (srcHubId.equals(destHubId)) {
            throw new IllegalArgumentException("출발 허브와 도착 허브는 같을 수 없습니다.");
        }
    }

    private HubRoute findActiveHubRoute(UUID hubRouteId) {
        return hubRouteRepository.findByHubRouteIdAndDeletedAtIsNull(hubRouteId)
                .orElseThrow(() -> new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND));
    }
}