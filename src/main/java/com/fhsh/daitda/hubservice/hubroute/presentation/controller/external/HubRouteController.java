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
     * 명세상 MASTER 권한이 필요한 API이므로 현재 인증 시스템에서는 ADMIN 권한으로 매핑하여 검증
     */
    @PostMapping
    public FindHubRouteResponse createHubRoute(@Valid @RequestBody CreateHubRouteRequest request,
                                               @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                               @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                               @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        FindHubRouteResult result = hubRouteCommandService.createHubRoute(request.toCommand(), authenticatedUser.userId());
        return FindHubRouteResponse.from(result);
    }

    /**
     * 삭제되지 않은 전체 허브 경로 목록을 조회
     * 명세상 ALL 권한 API이므로 인증된 사용자 역할이면 모두 허용
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
                .map(result -> ListHubRouteResponse.from(result))
                .toList();
    }

    /**
     * 허브 경로 ID 기준으로 단건 상세 정보를 조회
     * 명세상 ALL 권한 API이므로 인증된 사용자 역할이면 모두 허용
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
     * 명세상 ALL 권한 API이므로 인증된 사용자 역할이면 모두 허용
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
     * 허브 경로의 소요 시간과 이동 거리를 수정
     * 명세상 MASTER 권한이 필요한 API이므로 현재 인증 시스템에서는 ADMIN 권한으로 매핑하여 검증
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

        FindHubRouteResult result = hubRouteCommandService.updateHubRoute(hubRouteId, request.toCommand(), authenticatedUser.userId());
        return FindHubRouteResponse.from(result);
    }

    /**
     * 허브 경로를 논리 삭제
     * 지금은 삭제 성공 여부만 의미가 있어 별도 응답 본문 없이 void처리
     * 명세상 MASTER 권한이 필요한 API이므로 현재 인증 시스템에서는 ADMIN 권한으로 매핑하여 검증
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
