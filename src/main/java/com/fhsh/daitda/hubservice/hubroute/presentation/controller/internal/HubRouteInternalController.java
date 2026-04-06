package com.fhsh.daitda.hubservice.hubroute.presentation.controller.internal;

import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.service.query.HubRouteQueryService;
import com.fhsh.daitda.hubservice.hubroute.presentation.dto.response.FindHubRouteResponse;
import com.fhsh.daitda.response.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/hub-routes")
public class HubRouteInternalController {

    private final HubRouteQueryService hubRouteQueryService;

    public HubRouteInternalController(HubRouteQueryService hubRouteQueryService) {
        this.hubRouteQueryService = hubRouteQueryService;
    }

    /**
     * 출발 허브와 도착 허브 조합으로 허브 경로 조회
     */
    @GetMapping
    public FindHubRouteResponse getHubRoute(
            @RequestParam UUID srcHubId,
            @RequestParam UUID destHubId
    ) {
        FindHubRouteResult result = hubRouteQueryService.searchHubRoute(srcHubId, destHubId);
        return FindHubRouteResponse.from(result);
    }

    @GetMapping("/path")
    public CommonResponse<List<FindHubRouteResponse>> getHubRoutePath(@RequestParam UUID srcHubId, @RequestParam UUID destHubId) {
        List<FindHubRouteResult> results = hubRouteQueryService.getHubRoutePath(srcHubId, destHubId);

        List<FindHubRouteResponse> responses = results.stream()
                .map(result -> FindHubRouteResponse.from(result))
                .toList();

        return CommonResponse.success(responses);
    }
}
