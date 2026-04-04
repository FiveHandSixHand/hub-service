package com.fhsh.daitda.hubservice.hubinventory.presentation.controller.external;

import com.fhsh.daitda.common.config.security.SecurityHeaderConstants;
import com.fhsh.daitda.common.model.AuthenticatedUser;
import com.fhsh.daitda.common.util.AuthorizationUtils;
import com.fhsh.daitda.hubservice.hubinventory.application.command.CreateHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.command.UpdateHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.application.result.ListHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request.CreateHubInventoryRequest;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request.UpdateHubInventoryRequest;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.response.FindHubInventoryResponse;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.response.ListHubInventoryResponse;
import com.fhsh.daitda.hubservice.hubinventory.application.service.HubInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hub-inventories")
public class HubInventoryController {

    private final HubInventoryService hubInventoryService;

    public HubInventoryController(HubInventoryService hubInventoryService) {
        this.hubInventoryService = hubInventoryService;
    }

    /**
     * 허브 재고를 등록
     * 명세상 MASTER 권한이 필요한 API이므로 현재 인증 시스템에서는 ADMIN 권한으로 매핑하여 검증
     */
    @PostMapping
    public ResponseEntity<FindHubInventoryResponse> createHubInventory(@Valid @RequestBody CreateHubInventoryRequest request,
                                                                       @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                                                       @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                                       @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        CreateHubInventoryCommand command = CreateHubInventoryCommand.builder()
                .hubId(request.getHubId())
                .companyId(request.getCompanyId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResult result = hubInventoryService.createHubInventory(command, authenticatedUser.userId());
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 전체 허브 재고 목록을 조회
     * 명세상 ALL 권한 API이므로 인증된 사용자 역할이면 모두 허용
     */
    @GetMapping
    public ResponseEntity<List<ListHubInventoryResponse>> getHubInventories(@RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                                                            @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL,required = false) String email,
                                                                            @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateAllAccess(authenticatedUser);

        List<ListHubInventoryResult> results = hubInventoryService.getHubInventories();

        List<ListHubInventoryResponse> responses = results.stream()
                .map(response -> ListHubInventoryResponse.from(response))
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * hubId + companyId + productId 조합으로 특정 허브 재고를 조회
     * 명세상 ALL 권한 API이므로 인증된 사용자 역할이면 모두 허용
     */
    @GetMapping("/search")
    public ResponseEntity<FindHubInventoryResponse> searchHubInventory(@RequestParam UUID hubId,
                                                                       @RequestParam UUID companyId,
                                                                       @RequestParam UUID productId,
                                                                       @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                                                       @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                                       @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateAllAccess(authenticatedUser);

        FindHubInventoryResult result = hubInventoryService.searchHubInventory(hubId, companyId, productId);
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.ok(response);
    }

    /**
     * 허브 재고 ID 기준으로 단건 재고를 조회
     * 명세상 ALL 권한 API이므로 인증된 사용자 역할이면 모두 허용
     */
    @GetMapping("/{hubInventoryId}")
    public ResponseEntity<FindHubInventoryResponse> getHubInventory(@PathVariable UUID hubInventoryId,
                                                                    @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                                                    @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                                    @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateAllAccess(authenticatedUser);

        FindHubInventoryResult result = hubInventoryService.getHubInventory(hubInventoryId);
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.ok(response);
    }

    /**
     * 허브 재고 수량을 수정
     * 명세상 MASTER 권한이 필요한 API이므로 현재 인증 시스템에서는 ADMIN 권한으로 매핑하여 검증
     */
    @PatchMapping("/{hubInventoryId}")
    public ResponseEntity<FindHubInventoryResponse> updateHubInventory(@PathVariable UUID hubInventoryId,
                                                                       @Valid @RequestBody UpdateHubInventoryRequest request,
                                                                       @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                                                       @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                                       @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        UpdateHubInventoryCommand command = UpdateHubInventoryCommand.builder()
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResult result = hubInventoryService.updateHubInventory(hubInventoryId, command, authenticatedUser.userId());
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.ok(response);
    }

    /**
     * 허브 재고를 논리 삭제
     * 명세상 MASTER 권한이 필요한 API이므로 현재 인증 시스템에서는 ADMIN 권한으로 매핑하여 검증
     */
    @DeleteMapping("/{hubInventoryId}")
    public ResponseEntity<Void> deleteHubInventory(@PathVariable UUID hubInventoryId,
                                                   @RequestHeader(SecurityHeaderConstants.USER_ID) String userId,
                                                   @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                   @RequestHeader(SecurityHeaderConstants.USER_ROLE) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        AuthorizationUtils.validateMasterAccess(authenticatedUser);

        hubInventoryService.deleteHubInventory(hubInventoryId, authenticatedUser.userId());
        return ResponseEntity.noContent().build();
    }
}
