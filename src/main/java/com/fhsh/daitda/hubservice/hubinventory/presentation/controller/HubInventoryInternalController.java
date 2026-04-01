package com.fhsh.daitda.hubservice.hubinventory.presentation.controller;

import com.fhsh.daitda.hubservice.hubinventory.application.command.DecreaseHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.command.RestoreHubInventoryCommand;
import com.fhsh.daitda.hubservice.hubinventory.application.result.FindHubInventoryResult;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request.DecreaseHubInventoryRequest;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.request.RestoreHubInventoryRequest;
import com.fhsh.daitda.hubservice.hubinventory.presentation.dto.response.FindHubInventoryResponse;
import com.fhsh.daitda.hubservice.hubinventory.application.service.HubInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/hub-inventories")
public class HubInventoryInternalController {

    private final HubInventoryService hubInventoryService;

    public HubInventoryInternalController(HubInventoryService hubInventoryService) {
        this.hubInventoryService = hubInventoryService;
    }

    // 재고 차감
    @PatchMapping("/decrease")
    public ResponseEntity<FindHubInventoryResponse> decreaseHubInventory(@Valid @RequestBody DecreaseHubInventoryRequest request) {
        DecreaseHubInventoryCommand command = DecreaseHubInventoryCommand.builder()
                .hubInventoryId(request.getHubInventoryId())
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResult result = hubInventoryService.decreaseHubInventory(command, null);
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.ok(response);
    }

    // 재고 복원
    @PatchMapping("/restore")
    public ResponseEntity<FindHubInventoryResponse> restoreHubInventory(@Valid @RequestBody RestoreHubInventoryRequest request) {
        RestoreHubInventoryCommand command = RestoreHubInventoryCommand.builder()
                .hubInventoryId(request.getHubInventoryId())
                .quantity(request.getQuantity())
                .build();

        FindHubInventoryResult result = hubInventoryService.restoreHubInventory(command, null);
        FindHubInventoryResponse response = FindHubInventoryResponse.from(result);

        return ResponseEntity.ok(response);
    }
}
