package com.fhsh.daitda.hubservice.hubroute.application.service.command;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.exception.HubErrorCode;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
import com.fhsh.daitda.hubservice.hubroute.application.command.CreateHubRouteCommand;
import com.fhsh.daitda.hubservice.hubroute.application.command.UpdateHubRouteCommand;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import com.fhsh.daitda.hubservice.hubroute.domain.exception.HubRouteErrorCode;
import com.fhsh.daitda.hubservice.hubroute.domain.repository.HubRouteRepository;
import com.fhsh.daitda.hubservice.infrastructure.naver.client.NaverDirectionsClient;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class HubRouteCommandService {

    private final HubRouteRepository hubRouteRepository;
    private final HubRepository hubRepository;
    private final NaverDirectionsClient naverDirectionsClient;

    public HubRouteCommandService(HubRouteRepository hubRouteRepository, HubRepository hubRepository, NaverDirectionsClient naverDirectionsClient) {
        this.hubRouteRepository = hubRouteRepository;
        this.hubRepository = hubRepository;
        this.naverDirectionsClient = naverDirectionsClient;
    }


    // 허브 경로 생성
    @Transactional
    public FindHubRouteResult createHubRoute(CreateHubRouteCommand command, UUID createdBy) {
        validateDifferentHub(command.getSrcHubId(), command.getDestHubId());

        Hub srcHub = findActiveHub(command.getSrcHubId());
        Hub destHub = findActiveHub(command.getDestHubId());

        validateDuplicateHubRoute(command.getSrcHubId(), command.getDestHubId());

        NaverDirectionsClient.RouteMetrics metrics = naverDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        );

        HubRoute hubRoute = HubRoute.create(
                command.getSrcHubId(),
                command.getDestHubId(),
                metrics.durationMinutes(),
                metrics.distanceKilometers(),
                createdBy
        );

        try {
            HubRoute savedHubRoute = hubRouteRepository.saveAndFlush(hubRoute);
            return FindHubRouteResult.from(savedHubRoute);
        } catch (DataIntegrityViolationException e) {
            if (isUniqueConstraintViolation(e)) {
                throw new BusinessException(HubRouteErrorCode.HUB_ROUTE_CONFLICT);
            }
            throw e;
        }
    }

    // 허브 경로 정보 수정
    @Transactional
    public FindHubRouteResult updateHubRoute(UUID hubRouteId, UpdateHubRouteCommand command, UUID updatedBy) {
        HubRoute hubRoute = findActiveHubRoute(hubRouteId);

        Hub srcHub = findActiveHub(hubRoute.getSrcHubId());
        Hub destHub = findActiveHub(hubRoute.getDestHubId());

        NaverDirectionsClient.RouteMetrics metrics = naverDirectionsClient.getDrivingMetrics(
                srcHub.getLongitude(),
                srcHub.getLatitude(),
                destHub.getLongitude(),
                destHub.getLatitude()
        );

        hubRoute.updateRouteInfo(
                metrics.durationMinutes(),
                metrics.distanceKilometers(),
                updatedBy
        );

        return FindHubRouteResult.from(hubRoute);
    }

    // 허브 경로 논리 삭제
    @Transactional
    public void deleteHubRoute(UUID hubRouteId, UUID deletedBy) {
        HubRoute hubRoute = findActiveHubRoute(hubRouteId);
        hubRoute.softDelete(deletedBy);
    }

    private boolean isUniqueConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                return "uk_hub_route_src_dest".equals(constraintViolationException.getConstraintName());
            }
            cause = cause.getCause();
        }
        return false;
    }

    // 출발 허브 / 도착 허브 조합 중복 검증
    private void validateDuplicateHubRoute(UUID srcHubId, UUID destHubId) {
        boolean exists = hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(srcHubId, destHubId);

        if (exists) {
            throw new BusinessException(HubRouteErrorCode.HUB_ROUTE_CONFLICT);
        }
    }

    private void validateDifferentHub(UUID srcHubId, UUID destHubId) {
        if (srcHubId == null || destHubId == null) {
            throw new IllegalArgumentException("출발 허브와 도착 허브는 필수입니다.");
        }

        if (srcHubId.equals(destHubId)) {
            throw new IllegalArgumentException("출발 허브와 도착 허브는 같을 수 없습니다.");
        }
    }

    // 삭제되지 않은 허브 경로 조회
    private HubRoute findActiveHubRoute(UUID hubRouteId) {
        return hubRouteRepository.findByHubRouteIdAndDeletedAtIsNull(hubRouteId)
                .orElseThrow(() -> new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND));
    }

    // 삭제되지 않은 허브 조회
    private Hub findActiveHub(UUID hubId) {
        return hubRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new BusinessException(HubErrorCode.HUB_NOT_FOUND));
    }
}
