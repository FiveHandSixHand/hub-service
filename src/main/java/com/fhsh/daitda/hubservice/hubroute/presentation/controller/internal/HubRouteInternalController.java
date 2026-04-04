package com.fhsh.daitda.hubservice.hubroute.presentation.controller.internal;

import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.service.HubRouteService;
import com.fhsh.daitda.hubservice.hubroute.presentation.dto.response.FindHubRouteResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/hub-routes")
public class HubRouteInternalController {

    private final HubRouteService hubRouteService;

    public HubRouteInternalController(HubRouteService hubRouteService) {
        this.hubRouteService = hubRouteService;
    }

    /**
     * 출발 허브와 도착 허브 조합으로 허브 경로 조회
     */
    @GetMapping
    public FindHubRouteResponse getHubRoute(
            @RequestParam UUID srcHubId,
            @RequestParam UUID destHubId
    ) {
        FindHubRouteResult result = hubRouteService.searchHubRoute(srcHubId, destHubId);
        return FindHubRouteResponse.from(result);
    }


}
