package com.fhsh.daitda.hubservice.hubroute.application.service;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hub.domain.entity.Hub;
import com.fhsh.daitda.hubservice.hub.domain.exception.HubErrorCode;
import com.fhsh.daitda.hubservice.hub.domain.repository.HubRepository;
import com.fhsh.daitda.hubservice.hubroute.application.command.CreateHubRouteCommand;
import com.fhsh.daitda.hubservice.hubroute.application.command.UpdateHubRouteCommand;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.result.ListHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import com.fhsh.daitda.hubservice.hubroute.domain.exception.HubRouteErrorCode;
import com.fhsh.daitda.hubservice.hubroute.domain.repository.HubRouteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HubRouteService {

    private final HubRouteRepository hubRouteRepository;
    private final HubRepository hubRepository;

    public HubRouteService(HubRouteRepository hubRouteRepository, HubRepository hubRepository) {
        this.hubRouteRepository = hubRouteRepository;
        this.hubRepository = hubRepository;
    }

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
            throw new BusinessException(HubRouteErrorCode.HUB_ROUTE_CONFLICT);
        }
    }

    public List<ListHubRouteResult> getHubRoutes() {
        return hubRouteRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(hubRoute -> ListHubRouteResult.from(hubRoute))
                .toList();
    }

    public FindHubRouteResult getHubRoute(UUID hubRouteId) {
        HubRoute hubRoute = findActiveHubRoute(hubRouteId);
        return FindHubRouteResult.from(hubRoute);
    }

    public FindHubRouteResult searchHubRoute(UUID srcHubId, UUID destHubId) {
        HubRoute hubRoute = hubRouteRepository.findBySrcHubIdAndDestHubIdAndDeletedAtIsNull(srcHubId, destHubId)
                .orElseThrow(() -> new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND));

        return FindHubRouteResult.from(hubRoute);
    }

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

    @Transactional
    public void deleteHubRoute(UUID hubRouteId, String deletedBy) {
        HubRoute hubRoute = findActiveHubRoute(hubRouteId);
        hubRoute.softDelete(deletedBy);
    }

    private void validateDuplicateHubRoute(UUID srcHubId, UUID destHubId) {
        boolean exists = hubRouteRepository.existsBySrcHubIdAndDestHubIdAndDeletedAtIsNull(srcHubId, destHubId);

        if (exists) {
            throw new BusinessException(HubRouteErrorCode.HUB_ROUTE_CONFLICT);
        }
    }

    private HubRoute findActiveHubRoute(UUID hubRouteId) {
        return hubRouteRepository.findByHubRouteIdAndDeletedAtIsNull(hubRouteId)
                .orElseThrow(() -> new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND));
    }

    private Hub findActiveHub(UUID hubId) {
        return hubRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new BusinessException(HubErrorCode.HUB_NOT_FOUND));
    }
}
