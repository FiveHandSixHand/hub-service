package com.fhsh.daitda.hubservice.hubroute.presentation.controller.external;

import com.fhsh.daitda.common.config.security.SecurityHeaderConstants;
import com.fhsh.daitda.common.model.AuthenticatedUser;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.result.ListHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.service.HubRouteService;
import com.fhsh.daitda.hubservice.hubroute.presentation.dto.request.CreateHubRouteRequest;
import com.fhsh.daitda.hubservice.hubroute.presentation.dto.request.UpdateHubRouteRequest;
import com.fhsh.daitda.hubservice.hubroute.presentation.dto.response.FindHubRouteResponse;
import com.fhsh.daitda.hubservice.hubroute.presentation.dto.response.ListHubRouteResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hub-routes")
public class HubRouteController {

    private final HubRouteService hubRouteService;

    public HubRouteController(HubRouteService hubRouteService) {
        this.hubRouteService = hubRouteService;
    }

    /**
     * 출발 허브와 도착 허브 기준의 허브 경로 생성
     */
    @PostMapping
    public FindHubRouteResponse createHubRoute(@Valid @RequestBody CreateHubRouteRequest request,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_ID, required = false) String userId,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_ROLE, required = false) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);

        FindHubRouteResult result = hubRouteService.createHubRoute(request.toCommand(), authenticatedUser.userId());
        return FindHubRouteResponse.from(result);
    }

    /**
     * 삭제되지 않은 전체 허브 경로 목록 조회
     */
    @GetMapping
    public List<ListHubRouteResponse> getHubRoutes() {
        List<ListHubRouteResult> results = hubRouteService.getHubRoutes();

        return results.stream()
                .map(result -> ListHubRouteResponse.from(result))
                .toList();
    }

    /**
     * 허브 경로 ID 기준으로 단건 상세 정보 조회
     */
    @GetMapping("/{hubRouteId}")
    public FindHubRouteResponse getHubRoute(@PathVariable UUID hubRouteId) {
        FindHubRouteResult result = hubRouteService.getHubRoute(hubRouteId);
        return FindHubRouteResponse.from(result);
    }

    /**
     * 출발 허브와 도착 허브 조합으로 허브 경로 조회
     */
    @GetMapping("/search")
    public FindHubRouteResponse searchHubRoute(
            @RequestParam UUID srcHubId,
            @RequestParam UUID destHubId
    ) {
        FindHubRouteResult result = hubRouteService.searchHubRoute(srcHubId, destHubId);
        return FindHubRouteResponse.from(result);
    }

    /**
     * 허브 경로의 소요 시간과 이동 거리 수정
     */
    @PatchMapping("/{hubRouteId}")
    public FindHubRouteResponse updateHubRoute(@PathVariable UUID hubRouteId,
                                               @Valid @RequestBody UpdateHubRouteRequest request,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_ID, required = false) String userId,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_ROLE, required = false) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);

        FindHubRouteResult result = hubRouteService.updateHubRoute(hubRouteId, request.toCommand(), authenticatedUser.userId());
        return FindHubRouteResponse.from(result);
    }

    /**
     * 허브 경로를 논리 삭제
     * 지금은 삭제 성공 여부만 의미가 있어 별도 응답 본문 없이 void로 처리
     */
    @DeleteMapping("/{hubRouteId}")
    public void deleteHubRoute(@PathVariable UUID hubRouteId,
                               @RequestHeader(value = SecurityHeaderConstants.USER_ID, required = false) String userId,
                               @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                               @RequestHeader(value = SecurityHeaderConstants.USER_ROLE, required = false) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);

        hubRouteService.deleteHubRoute(hubRouteId, authenticatedUser.userId());
    }
}
