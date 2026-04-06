package com.fhsh.daitda.hubservice.hubroute.presentation.controller.external;

import com.fhsh.daitda.common.config.security.SecurityHeaderConstants;
import com.fhsh.daitda.common.model.AuthenticatedUser;
import com.fhsh.daitda.common.util.AuthorizationUtils;
import com.fhsh.daitda.hubservice.hubroute.application.result.FindHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.result.ListHubRouteResult;
import com.fhsh.daitda.hubservice.hubroute.application.service.command.HubRouteCommandService;
import com.fhsh.daitda.hubservice.hubroute.application.service.query.HubRouteQueryService;
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

    private final HubRouteCommandService hubRouteCommandService;
    private final HubRouteQueryService hubRouteQueryService;

    public HubRouteController(HubRouteCommandService hubRouteCommandService, HubRouteQueryService hubRouteQueryService) {
        this.hubRouteCommandService = hubRouteCommandService;
        this.hubRouteQueryService = hubRouteQueryService;
    }

    /**
     * 출발 허브와 도착 허브 기준의 허브 경로를 생성
     *
     * 거리와 시간은 요청에서 직접 받지 않고
     * Naver Directions 계산 결과를 기반으로 저장
     */
    @PostMapping
    public FindHubRouteResponse createHubRoute(@Valid @RequestBody CreateHubRouteRequest request,
                                               @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                               @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        FindHubRouteResult result = hubRouteCommandService.createHubRoute(
                request.toCommand(),
                authenticatedUser.userId()
        );

        return FindHubRouteResponse.from(result);
    }

    /**
     * 삭제되지 않은 전체 허브 경로 목록을 조회
     */
    @GetMapping
    public List<ListHubRouteResponse> getHubRoutes(@RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                                   @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                   @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateAllAccess(authenticatedUser);

        List<ListHubRouteResult> results = hubRouteQueryService.getHubRoutes();

        return results.stream()
                .map(ListHubRouteResponse::from)
                .toList();
    }

    /**
     * 허브 경로 ID 기준으로 단건 상세 정보를 조회
     */
    @GetMapping("/{hubRouteId}")
    public FindHubRouteResponse getHubRoute(@PathVariable UUID hubRouteId,
                                            @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                            @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                            @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateAllAccess(authenticatedUser);

        FindHubRouteResult result = hubRouteQueryService.getHubRoute(hubRouteId);
        return FindHubRouteResponse.from(result);
    }

    /**
     * 출발 허브와 도착 허브 조합으로 허브 경로를 조회
     */
    @GetMapping("/search")
    public FindHubRouteResponse searchHubRoute(@RequestParam UUID srcHubId,
                                               @RequestParam UUID destHubId,
                                               @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                               @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateAllAccess(authenticatedUser);

        FindHubRouteResult result = hubRouteQueryService.searchHubRoute(srcHubId, destHubId);
        return FindHubRouteResponse.from(result);
    }

    /**
     * 허브 경로를 재계산
     *
     * 빈 DTO를 유지하되, 실제로는 request 값으로 수정하지 않고
     * 기존 route의 src/dest 허브를 기준으로 Naver Directions를 다시 호출해
     * 거리와 시간을 재계산
     */
    @PatchMapping("/{hubRouteId}")
    public FindHubRouteResponse updateHubRoute(@PathVariable UUID hubRouteId,
                                               @Valid @RequestBody UpdateHubRouteRequest request,
                                               @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                               @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        FindHubRouteResult result = hubRouteCommandService.updateHubRoute(
                hubRouteId,
                request.toCommand(),
                authenticatedUser.userId()
        );

        return FindHubRouteResponse.from(result);
    }

    /**
     * 허브 경로를 논리 삭제
     * 지금은 삭제 성공 여부만 의미가 있어 별도 응답 본문 없이 void 처리
     */
    @DeleteMapping("/{hubRouteId}")
    public void deleteHubRoute(@PathVariable UUID hubRouteId,
                               @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                               @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                               @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        hubRouteCommandService.deleteHubRoute(hubRouteId, authenticatedUser.userId());
    }
}