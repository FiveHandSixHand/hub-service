package com.fhsh.daitda.hubservice.hub.presentation.controller.external;

import com.fhsh.daitda.common.config.security.SecurityHeaderConstants;
import com.fhsh.daitda.common.model.AuthenticatedUser;
import com.fhsh.daitda.common.util.AuthorizationUtils;
import com.fhsh.daitda.hubservice.hub.application.command.CreateHubCommand;
import com.fhsh.daitda.hubservice.hub.application.command.UpdateHubCommand;
import com.fhsh.daitda.hubservice.hub.application.result.FindHubResult;
import com.fhsh.daitda.hubservice.hub.application.result.ListHubResult;
import com.fhsh.daitda.hubservice.hub.application.service.command.HubCommandService;
import com.fhsh.daitda.hubservice.hub.application.service.query.HubQueryService;
import com.fhsh.daitda.hubservice.hub.presentation.dto.request.HubCreateRequest;
import com.fhsh.daitda.hubservice.hub.presentation.dto.request.HubUpdateRequest;
import com.fhsh.daitda.hubservice.hub.presentation.dto.response.HubResponse;
import com.fhsh.daitda.hubservice.hub.presentation.dto.response.ListHubResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubs")
public class HubController {

    private final HubCommandService hubCommandService;
    private final HubQueryService hubQueryService;

    public HubController(HubCommandService hubCommandService, HubQueryService hubQueryService) {
        this.hubCommandService = hubCommandService;
        this.hubQueryService = hubQueryService;
    }

    /**
     * 허브를 생성
     * 외부 요청은 Gateway를 통해 진입하므로 사용자 헤더를 받아 createdBy에 반영
     * 명세상 MASTER 권한이 필요한 API이므로 ADMIN 권한으로 매핑하여 검증
     */
    @PostMapping
    public ResponseEntity<HubResponse> createHub(
            @Valid @RequestBody HubCreateRequest request,
            @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
            @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
            @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role
    ) {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        CreateHubCommand command = CreateHubCommand.builder()
                .hubName(request.getHubName())
                .hubAddress(request.getHubAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isCentral(request.getIsCentral())
                .build();

        FindHubResult result = hubCommandService.createHub(command, authenticatedUser.userId());
        HubResponse response = HubResponse.from(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 전체 허브 목록을 조회
     * 명세상 ALL 권한 API이므로 인증된 사용자 역할이면 모두 허용
     */
    @GetMapping
    public ResponseEntity<List<ListHubResponse>> getHubs(@RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                                         @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                         @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateAllAccess(authenticatedUser);

        List<ListHubResult> results = hubQueryService.getHubs();

        List<ListHubResponse> responses = results
                .stream()
                .map(hub -> ListHubResponse.from(hub))
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * 허브 ID 기준으로 단건 허브를 조회
     * 명세상 ALL 권한 API이므로 인증된 사용자 역할이면 모두 허용
     */
    @GetMapping("/{hubId}")
    public ResponseEntity<HubResponse> getHub(@PathVariable UUID hubId,
                                              @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                              @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                              @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateAllAccess(authenticatedUser);

        FindHubResult result = hubQueryService.getHub(hubId);
        HubResponse response = HubResponse.from(result);

        return ResponseEntity.ok(response);
    }

    /**
     * 허브 정보를 수정
     * 명세상 MASTER 권한이 필요한 API이므로 ADMIN 권한으로 매핑하여 검증
     */
    @PatchMapping("/{hubId}")
    public ResponseEntity<HubResponse> updateHub(@PathVariable UUID hubId,
                                                 @Valid @RequestBody HubUpdateRequest request,
                                                 @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                                 @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                 @RequestHeader(SecurityHeaderConstants.USER_ROLE)String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        UpdateHubCommand command = UpdateHubCommand.builder()
                .hubName(request.getHubName())
                .hubAddress(request.getHubAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isCentral(request.getIsCentral())
                .build();

        FindHubResult result = hubCommandService.updateHub(hubId, command, authenticatedUser.userId());
        HubResponse response = HubResponse.from(result);

        return ResponseEntity.ok(response);
    }

    /**
     * 허브를 논리 삭제
     * 명세상 MASTER 권한이 필요한 API이므로 ADMIN 권한으로 매핑하여 검증
     */
    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(@PathVariable UUID hubId,
                                          @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                          @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                          @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        hubCommandService.deleteHub(hubId, authenticatedUser.userId());
        return ResponseEntity.noContent().build();
    }
}
