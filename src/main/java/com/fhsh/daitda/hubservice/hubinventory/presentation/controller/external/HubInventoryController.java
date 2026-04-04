package com.fhsh.daitda.hubservice.hubinventory.presentation.controller.external;

import com.fhsh.daitda.common.config.security.SecurityHeaderConstants;
import com.fhsh.daitda.common.model.AuthenticatedUser;
import com.fhsh.daitda.hubservice.hubinventory.application.command.CreateHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.command.UpdateHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
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

    // 재고 생성
    @PostMapping
    public ResponseEntity<FindHubInventoryResponse> createHubInventory(@Valid @RequestBody CreateHubInventoryRequest request,
                                                                       @RequestHeader(value = SecurityHeaderConstants.USER_ID, required = false) String userId,
                                                                       @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                                       @RequestHeader(value = SecurityHeaderConstants.USER_ROLE, required = false) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);

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

    // 전체 재고 목록 조회
    @GetMapping
    public ResponseEntity<List<ListHubInventoryResponse>> getHubInventories() {
        List<ListHubInventoryResponse> responses = hubInventoryService.getHubInventories()
                .stream()
                .map(response -> ListHubInventoryResponse.from(response))
                .toList();

        return ResponseEntity.ok(responses);
    }

    // hubId + companyId + productId 조합으로 특정 허브 재고 조회
    @GetMapping("/search")
    public ResponseEntity<FindHubInventoryResponse> searchHubInventory(@RequestParam UUID hubId,
                                                                         @RequestParam UUID companyId,
                                                                         @RequestParam UUID productId) {
        FindHubInventoryResult result = hubInventoryService.searchHubInventory(hubId, companyId, productId);
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.ok(response);
    }

    // 특정 허브 재고 상세 조회
    @GetMapping("/{hubInventoryId}")
    public ResponseEntity<FindHubInventoryResponse> getHubInventory(@PathVariable UUID hubInventoryId) {
        FindHubInventoryResult result = hubInventoryService.getHubInventory(hubInventoryId);
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.ok(response);
    }

    // 재고 수량 직접 수정
    @PatchMapping("/{hubInventoryId}")
    public ResponseEntity<FindHubInventoryResponse> updateHubInventory(@PathVariable UUID hubInventoryId,
                                                                       @Valid @RequestBody UpdateHubInventoryRequest request,
                                                                       @RequestHeader(value = SecurityHeaderConstants.USER_ID, required = false) String userId,
                                                                       @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                                       @RequestHeader(value = SecurityHeaderConstants.USER_ROLE, required = false) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);

        UpdateHubInventoryCommand command = UpdateHubInventoryCommand.builder()
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResult result = hubInventoryService.updateHubInventory(hubInventoryId, command, authenticatedUser.userId());
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.ok(response);
    }

    // 재고 논리 삭제
    @DeleteMapping("/{hubInventoryId}")
    public ResponseEntity<Void> deleteHubInventory(@PathVariable UUID hubInventoryId,
                                                   @RequestHeader(value = SecurityHeaderConstants.USER_ID, required = false) String userId,
                                                   @RequestHeader(value = SecurityHeaderConstants.USER_EMAIL, required = false) String email,
                                                   @RequestHeader(value = SecurityHeaderConstants.USER_ROLE, required = false) String role)
    {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.fromHeaders(userId, email, role);
        hubInventoryService.deleteHubInventory(hubInventoryId, authenticatedUser.userId());
        return ResponseEntity.noContent().build();
    }
}
