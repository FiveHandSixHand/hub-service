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

    public HubRouteCommandService(HubRouteRepository hubRouteRepository, HubRepository hubRepository) {
        this.hubRouteRepository = hubRouteRepository;
        this.hubRepository = hubRepository;
    }

    // 허브 경로 생성
    @Transactional
    public FindHubRouteResult createHubRoute(CreateHubRouteCommand command, String createdBy) {
        findActiveHub(command.getSrcHubId());
        findActiveHub(command.getDestHubId());
        validateDuplicateHubRoute(command.getSrcHubId(), command.getDestHubId());

        HubRoute hubRoute = HubRoute.create(
                command.getSrcHubId(),
                command.getDestHubId(),
                command.getDurationTime(),
                command.getDistance(),
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
    public FindHubRouteResult updateHubRoute(UUID hubRouteId, UpdateHubRouteCommand command, String updatedBy) {
        HubRoute hubRoute = findActiveHubRoute(hubRouteId);

        hubRoute.updateRouteInfo(
                command.getDurationTime(),
                command.getDistance(),
                updatedBy
        );

        return FindHubRouteResult.from(hubRoute);
    }

    // 허브 경로 논리 삭제
    @Transactional
    public void deleteHubRoute(UUID hubRouteId, String deletedBy) {
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
