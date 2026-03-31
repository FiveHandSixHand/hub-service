package com.daitda.hubservice.hubinventory.presentation.controller;

import com.daitda.hubservice.hubinventory.application.dto.command.CreateHubInventoryCommand;
import com.daitda.hubservice.hubinventory.application.dto.command.UpdateHubInventoryCommand;
import com.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import com.daitda.hubservice.hubinventory.presentation.dto.request.CreateHubInventoryRequest;
import com.daitda.hubservice.hubinventory.presentation.dto.request.UpdateHubInventoryRequest;
import com.daitda.hubservice.hubinventory.presentation.dto.response.FindHubInventoryResponse;
import com.daitda.hubservice.hubinventory.presentation.dto.response.ListHubInventoryResponse;
import com.daitda.hubservice.hubinventory.application.service.HubInventoryService;
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
    public ResponseEntity<FindHubInventoryResponse> createHubInventory(@Valid @RequestBody CreateHubInventoryRequest request) {

        CreateHubInventoryCommand command = CreateHubInventoryCommand.builder()
                .hubId(request.getHubId())
                .companyId(request.getCompanyId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResult result = hubInventoryService.createHubInventory(command, null);
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
                                                                       @Valid @RequestBody UpdateHubInventoryRequest request) {
        UpdateHubInventoryCommand command = UpdateHubInventoryCommand.builder()
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResult result = hubInventoryService.updateHubInventory(hubInventoryId, command, null);
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.ok(response);
    }

    // 재고 논리 삭제
    @DeleteMapping("/{hubInventoryId}")
    public ResponseEntity<Void> deleteHubInventory(@PathVariable UUID hubInventoryId) {
        hubInventoryService.deleteHubInventory(hubInventoryId, null);
        return ResponseEntity.noContent().build();
    }
}
