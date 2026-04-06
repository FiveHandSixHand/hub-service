package com.fhsh.daitda.hubservice.hub.presentation.controller.internal;

import com.fhsh.daitda.hubservice.hub.application.result.FindHubResult;
import com.fhsh.daitda.hubservice.hub.application.service.query.HubQueryService;
import com.fhsh.daitda.hubservice.hub.presentation.dto.response.HubResponse;
import com.fhsh.daitda.response.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/hubs")
public class HubInternalController {

    private final HubQueryService hubQueryService;

    public HubInternalController(HubQueryService hubQueryService) {
        this.hubQueryService = hubQueryService;
    }

    @GetMapping("/{hubId}")
    public CommonResponse<HubResponse> getHub(@PathVariable UUID hubId) {
        FindHubResult result = hubQueryService.getHub(hubId);
        return CommonResponse.success(HubResponse.from(result));
    }
}
