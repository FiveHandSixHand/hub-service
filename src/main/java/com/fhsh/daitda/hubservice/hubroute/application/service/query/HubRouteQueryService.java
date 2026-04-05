package com.fhsh.daitda.hubservice.hubroute.application.service.query;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.result.ListHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import com.fhsh.daitda.hubservice.hubroute.domain.exception.HubRouteErrorCode;
import com.fhsh.daitda.hubservice.hubroute.domain.repository.HubRouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class HubRouteQueryService {

    /**
     * 릴레이 배송으로 전환할 기준 거리
     */
    private static final BigDecimal RELAY_THRESHOLD_KM = new BigDecimal("200");

    private final HubRouteRepository hubRouteRepository;

    public HubRouteQueryService(HubRouteRepository hubRouteRepository) {
        this.hubRouteRepository = hubRouteRepository;
    }

    // 삭제되지 않은 전체 허브 경로 목록 조회
    public List<ListHubRouteResult> getHubRoutes() {
        return hubRouteRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(hubRoute -> ListHubRouteResult.from(hubRoute))
                .toList();
    }

    // 허브 경로 ID 기준으로 단건 경로 조회
    public FindHubRouteResult getHubRoute(UUID hubRouteId) {
        HubRoute hubRoute = findActiveHubRoute(hubRouteId);
        return FindHubRouteResult.from(hubRoute);
    }

    //  출발 허브와 도착 허브 조합으로 개별 구간 route를 조회
    public FindHubRouteResult searchHubRoute(UUID srcHubId, UUID destHubId) {
        HubRoute hubRoute = hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(srcHubId, destHubId)
                .orElseThrow(() -> new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND));

        return FindHubRouteResult.from(hubRoute);
    }

    /**
     * 최종 배송 경로(path)를 조회
     *
     * 정책:
     * - 직행 route가 존재하고 거리가 200km 미만이면 1개짜리 리스트 반환
     * - 직행 route가 200km 이상이거나 직행 route가 없으면 릴레이 경로를 계산해서 리스트 반환
     */
    public List<FindHubRouteResult> getHubRoutePath(UUID srcHubId, UUID destHubId) {
        validateDifferentHub(srcHubId, destHubId);

        Optional<HubRoute> directRoute = hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(srcHubId, destHubId);

        if (directRoute.isPresent() && directRoute.get().getDistance().compareTo(RELAY_THRESHOLD_KM) < 0) {
            return List.of(FindHubRouteResult.from(directRoute.get()));
        }

        return findRelayPath(srcHubId, destHubId).stream()
                .map(hubRoute -> FindHubRouteResult.from(hubRoute))
                .toList();
    }

    /**
     * 릴레이 경로를 계산
     *
     * active route를 그래프로 보는 이유
     * - hub_route row는 개별 구간 edge 역할을 하기 때문
     * - 최종 배송 경로(path)는 여러 edge를 이어 붙인 결과이기 때문
     *
     * 다익스트라를 쓰는 이유:
     * - distance가 가중치인 최단 경로 문제로 볼 수 있기 때문
     * - 현재 요구사항에서는 가장 자연스럽고 구현도 안정적인 방식이기 때문
     */
    private List<HubRoute> findRelayPath(UUID srcHubId, UUID destHubId) {
        List<HubRoute> routes = hubRouteRepository.findAllByDeletedAtIsNull();

        Map<UUID, List<HubRoute>> graph = buildGraph(routes);
        Map<UUID, BigDecimal> distanceMap = new HashMap<>();
        Map<UUID, HubRoute> previousRouteMap = new HashMap<>();

        PriorityQueue<RouteNode> pq = new PriorityQueue<>((o1, o2) ->
            o1.distance().compareTo(o2.distance)
        );

        distanceMap.put(srcHubId, BigDecimal.ZERO);
        pq.offer(new RouteNode(srcHubId, BigDecimal.ZERO));

        while (!pq.isEmpty()) {
            RouteNode current = pq.poll();

            BigDecimal knownDistance = distanceMap.getOrDefault(
                    current.hubId,
                    BigDecimal.valueOf(Double.MAX_VALUE)
            );

            if (current.distance().compareTo(knownDistance) > 0) {
                continue;
            }

            if (current.hubId().equals(destHubId)) {
                break;
            }

            List<HubRoute> nextRoutes = graph.getOrDefault(current.hubId, Collections.emptyList());

            for (HubRoute nextRoute : nextRoutes) {
                UUID nextHubId = nextRoute.getDestHubId();
                BigDecimal nextDistance = current.distance().add(nextRoute.getDistance());

                BigDecimal recordedDistance = distanceMap.get(nextHubId);

                if (recordedDistance == null || nextDistance.compareTo(recordedDistance) < 0) {
                    distanceMap.put(nextHubId, nextDistance);
                    previousRouteMap.put(nextHubId, nextRoute);
                    pq.offer(new RouteNode(nextHubId, nextDistance));
                }
            }
        }

        if (!previousRouteMap.containsKey(destHubId)) {
            throw new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
        }
        return reconstructPath(previousRouteMap, srcHubId, destHubId);
    }

    /**
     * active route row들을 src 기준 그래프 형태로 변환
     *
     * 기능:
     * - key: 출발 허브 ID
     * - value: 그 허브에서 출발하는 route 목록
     *
     * 예시:
     * - 서울 -> 대전
     * - 서울 -> 강릉
     *
     * 그러면 graph.get(서울) 은
     * [서울->대전, 서울->강릉]
     *
     * 별도 메서드로 분리한 이유
     * - path 계산 로직 본문이 너무 길어지지 않게 하기 위해
     * - "route 목록을 그래프로 본다"는 의도를 메서드 이름으로 드러내기 위해
     */
    private Map<UUID, List<HubRoute>> buildGraph(List<HubRoute> routes) {
        Map<UUID, List<HubRoute>> graph = new HashMap<>();

        for (HubRoute route : routes) {
            graph.computeIfAbsent(route.getSrcHubId(), Key -> new ArrayList<>()).add(route);
        }
        return graph;
    }

    private List<HubRoute> reconstructPath(Map<UUID, HubRoute> previousRouteMap,
                                           UUID srcHubId,
                                           UUID destHubId)
    {
        List<HubRoute> path = new ArrayList<>();
        UUID currentHubId = destHubId;

        while (!currentHubId.equals(srcHubId)) {
            HubRoute route = previousRouteMap.get(currentHubId);

            if (route == null) {
                throw new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
            }

            path.add(route);
            currentHubId = route.getSrcHubId();
        }

        Collections.reverse(path);
        return path;
    }

    private void validateDifferentHub(UUID srcHubId, UUID destHubId) {
        if (srcHubId.equals(destHubId)) {
            throw new IllegalArgumentException("출발 허브와 도착 허브는 같을 수 없습니다.");
        }
    }

    private HubRoute findActiveHubRoute(UUID hubRouteId) {
        return hubRouteRepository.findByHubRouteIdAndDeletedAtIsNull(hubRouteId)
                .orElseThrow(() -> new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND));
    }

    /**
     * 우선순위 큐에 넣을 노드
     *
     * hubId:
     * - 현재 도착해 있는 허브
     *
     * distance:
     * - 출발 허브부터 현재 허브까지 누적 거리
     *
     * 필요한 이유:
     * - 다익스트라에서 "가장 짧은 후보부터 먼저 처리"해야 하기 때문
     */
    private record RouteNode(UUID hubId, BigDecimal distance) {
    }
}
