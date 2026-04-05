package com.fhsh.daitda.hubservice.hubroute.application.service.query;

import com.fhsh.daitda.exception.BusinessException;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.result.ListHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.domain.entity.HubRoute;
import com.fhsh.daitda.hubservice.hubroute.domain.exception.HubRouteErrorCode;
import com.fhsh.daitda.hubservice.hubroute.domain.repository.HubRouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HubRouteQueryService {

    private final HubRouteRepository hubRouteRepository;

    public HubRouteQueryService(HubRouteRepository hubRouteRepository) {
        this.hubRouteRepository = hubRouteRepository;
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

    private HubRoute findActiveHubRoute(UUID hubRouteId) {
        return hubRouteRepository.findByHubRouteIdAndDeletedAtIsNull(hubRouteId)
                .orElseThrow(() -> new BusinessException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND));
    }
}
